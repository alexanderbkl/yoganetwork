package com.amit.yoganet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.amit.yoganet.constants.Constant;
import com.amit.yoganet.fragments.*;
import com.amit.yoganet.notifications.FirebaseMessaging;
import com.amit.yoganet.notifications.Token;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import static com.amit.yoganet.constants.Constant.LOCATION_SERVICES;

public class DashboardActivity extends AppCompatActivity implements
        MapFragment.OnFragmentInteractionListener {



    public static final int INTERVAL = 60000;


    //firebase authentication
    FirebaseAuth firebaseAuth;
    Toolbar toolbar;

    private String mUID, fragPos, prevFrag;
    private String extra = "";
    private boolean prevAct = false;

    private    BottomNavigationView navigationView;


    private LocationCallback mLocationCallback;
    private final String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final int REQUEST_CHECK_SETTINGS = 1;
    private PostsFragment fragment1;

    private ProfileFragment fragment2;
    private UsersCardStackFragment fragment3;
    private ChatListFragment fragment4;
    private NotificationsFragment fragment5;
    private GroupChatsFragment fragment6;
    private MapFragment fragment7;

    public DashboardActivity() {
    }

    public DashboardActivity(PostsFragment fragment1, ProfileFragment fragment2, UsersCardStackFragment fragment3, ChatListFragment fragment4, NotificationsFragment fragment5, GroupChatsFragment fragment6, MapFragment fragment7) {
        this.fragment1 = fragment1;
        this.fragment2 = fragment2;
        this.fragment3 = fragment3;
        this.fragment4 = fragment4;
        this.fragment5 = fragment5;
        this.fragment6 = fragment6;
        this.fragment7 = fragment7;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);
        //Actionbar and its title
        toolbar = findViewById(R.id.toolbar_main);
        //   toolbar.inflateMenu(R.menu.menu_main);
        //init
        firebaseAuth = FirebaseAuth.getInstance();
        //bottom navigation
        navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.inicio);

        checkUserStatus();

        initLocation();





        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                fragPos = "0";
                prevFrag = "0";
             //   navigationView.setSelectedItemId(R.id.nav_home);
                addFragments();
            } else{
                fragPos= extras.getString("fragPos");
                extra= extras.getString("extra");

                prevFrag = fragPos;
                prevAct = true;
                addFragments();
            }
        } else {
            fragPos= (String) savedInstanceState.getSerializable("fragPos");
            if (fragPos == null) {
                fragPos = "0";
                prevFrag = "0";
            }
            addFragments();
        }







    }



    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken);
    }

    //if PostsFragment was loaded at least once, maintain it at the background so that the dada is not lost
    private final BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    //handle item clicks
                    int itemId = item.getItemId();
                   if (itemId == R.id.nav_home) {
                            //save fragment position
                            if (fragPos != null && !fragPos.equals("0")) {
                                //home fragment transaction
                                toolbar.setTitle(R.string.inicio); //change actionbar title
                                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                                if (Integer.parseInt(fragPos) > 0) {
                                    ft1.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
                                } else {
                                    ft1.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                                }
                                hideExistingFragments(ft1, 0);
                                prevFrag = fragPos;
                                fragPos = "0";
                            } else {
                                fragPos = "0";
                                //home fragment transaction
                                addFragments();
                            }
                            return true;
                        }
                        else if (itemId == R.id.nav_profile) {
                            if (fragPos != null && !fragPos.equals("1")) {
                                //profile fragment transaction
                                //save fragment position
                                toolbar.setTitle(R.string.profile); //change actionbar title
                                FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                                if (Integer.parseInt(fragPos) > 1) {
                                    ft2.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
                                } else {
                                    ft2.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                                }
                                hideExistingFragments(ft2, 1);
                                prevFrag = fragPos;
                                fragPos = "1";
                            } else {
                                fragPos = "1";
                                //profile fragment transaction
                                addFragments();
                            }
                            return true;
                        }
                        else if (itemId == R.id.nav_users) {
                            if (fragPos != null && !fragPos.equals("2")) {
                                //users fragment transaction
                                toolbar.setTitle(R.string.users); //change actionbar title
                                FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                                if (Integer.parseInt(fragPos) > 2) {
                                    ft3.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
                                } else {
                                    ft3.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                                }
                                hideExistingFragments(ft3, 2);
                                prevFrag = fragPos;
                                fragPos = "2";
                            } else {
                                fragPos = "2";
                                //users fragment transaction
                                addFragments();

                            }
                            return true;
                        }
                        else if (itemId == R.id.nav_chat) {
                            if (fragPos != null && !fragPos.equals("3")) {
                                //users fragment transaction

                                toolbar.setTitle(getString(R.string.chats)); //change actionbar title
                                FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                                if (Integer.parseInt(fragPos) > 3) {
                                    ft4.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
                                } else {
                                    ft4.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                                }
                                hideExistingFragments(ft4, 3);
                                prevFrag = fragPos;
                                fragPos = "3";
                            } else {
                                fragPos = "3";
                                //users fragment transaction
                                addFragments();
                            }
                            return true;
                        }
                        else if (itemId == R.id.nav_more) {
                            prevAct = false;
                            showMoreOptions();
                            return true;
                        
                    }
                    return false;
                }
            };


    private void showMoreOptions() {
        //popup menu to show more options
        PopupMenu popupMenu = new PopupMenu(DashboardActivity.this, navigationView, Gravity.END);
        //items to show in menu
        popupMenu.getMenu().add(Menu.NONE, 0, 0, R.string.notificaciones);
        popupMenu.getMenu().add(Menu.NONE, 1, 0, R.string.grupos);
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "Maps");

        //menu clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override

            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {

                    if (fragPos != null && !fragPos.equals("4")) {
                        //notification clicked

                        //notifications fragment transaction
                        //           toolbar.setVisibility(View.GONE);

                        toolbar.setTitle(R.string.notificaciones); //change actionbar title
                        FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                        ft5.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                        hideExistingFragments(ft5, 4);
                        prevFrag = fragPos;
                        fragPos = "4";
                    } else {
                        fragPos = "4";
                        //notifications fragment transaction
                        addFragments();
                    }






                }
                else if (id == 1) {
                    if (fragPos != null && !fragPos.equals("5")) {
                        //group chats clicked
                        //group chats fragment transaction
                        toolbar.setTitle(R.string.grupos); //change actionbar title
                        FragmentTransaction ft6 = getSupportFragmentManager().beginTransaction();
                        ft6.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                        hideExistingFragments(ft6, 5);
                        prevFrag = fragPos;
                        fragPos = "5";
                    } else {
                        fragPos = "5";
                        //group chats fragment transaction
                        addFragments();
                    }
                }  else if (id == 2) {
                    if (fragPos!=null && !fragPos.equals("6")) {
                        //group chats clicked
                        //group chats fragment transaction
                        toolbar.setTitle(getString(R.string.mapas)); //change actionbar title
                        FragmentTransaction ft7 = getSupportFragmentManager().beginTransaction();
                        ft7.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                        hideExistingFragments(ft7, 6);
                        prevFrag = fragPos;
                        fragPos = "6";
                    } else {
                        fragPos = "6";
                        //group chats fragment transaction
                        addFragments();
                    }
                    //        toolbar.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
        //check if activity redirection is false
        if (!prevAct) {
            popupMenu.show();
          //  prevAct = false;
        }




    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
            mUID = user.getUid();

            //save uid of currently signed in user in shared preferences
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

            //update token
            updateToken(FirebaseMessaging.getToken(this));
        }
        else {
            //user not signed in, go to mainactivity
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {

        if (getFragmentManager().getBackStackEntryCount() > 0 ){
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else if ((prevFrag != null)) {
            switch (prevFrag) {
                case "0":
                    fragPos = "0";
                    //home fragment transaction
                    toolbar.setTitle(getString(R.string.inicio)); //change actionbar title
                    navigationView.setSelectedItemId(R.id.nav_home);
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    hideExistingFragments(ft, 0);

                    break;




                case "1":
                    fragPos = "1";
                    //profile fragment transaction
                    toolbar.setTitle(getString(R.string.perfil)); //change actionbar title
                    navigationView.setSelectedItemId(R.id.nav_profile);
                    FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                    hideExistingFragments(ft2, 1);
                    break;
                case "2":
                    fragPos = "2";
                    //users fragment transaction
                    toolbar.setTitle(getString(R.string.users)); //change actionbar title
                    navigationView.setSelectedItemId(R.id.nav_users);
                    FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                    hideExistingFragments(ft3, 2);
                    break;
                case "3":
                    fragPos = "3";
                    //chats fragment transaction
                    toolbar.setTitle(getString(R.string.chats)); //change actionbar title
                    navigationView.setSelectedItemId(R.id.nav_chat);
                    FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                    hideExistingFragments(ft4, 3);
                    break;
                case "4":
                    fragPos = "4";
                    //notifications fragment transaction
                    toolbar.setTitle(getString(R.string.notificaciones)); //change actionbar title
                    navigationView.setSelectedItemId(R.id.nav_more);
                    FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                    hideExistingFragments(ft5, 4);
                    break;
                case "5":
                    fragPos = "5";
                    //groups fragment transaction
                    toolbar.setTitle(getString(R.string.grupos)); //change actionbar title
                    navigationView.setSelectedItemId(R.id.nav_more);
                    FragmentTransaction ft6 = getSupportFragmentManager().beginTransaction();
                    hideExistingFragments(ft6, 5);
                    break;
                case "6":
                    fragPos = "6";
                    //maps fragment transaction
                    toolbar.setTitle(getString(R.string.grupos)); //change actionbar title
                    navigationView.setSelectedItemId(R.id.nav_more);
                    FragmentTransaction ft7 = getSupportFragmentManager().beginTransaction();
                    hideExistingFragments(ft7, 6);
                    break;
            }

        }

        else {
            super.onBackPressed();
        }
    }

    private void hideExistingFragments(FragmentTransaction ft, int fragment) {
        switch (fragment) {
            case 0:


                if (fragment2 != null) {ft.hide(fragment2);}
                if (fragment3 != null) {ft.hide(fragment3);}
                if (fragment4 != null) {ft.hide(fragment4);}
                if (fragment5 != null) {ft.hide(fragment5);}
                if (fragment6 != null) {ft.hide(fragment6);}
                if (fragment7 != null) {ft.hide(fragment7);}
                if (fragment1 == null) {
                    ft.commit();
                    fragment1 = new PostsFragment();
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1
                            .add(R.id.content, fragment1, "");
                    ft1.show(fragment1).addToBackStack(null).commit();
                } else {
                    ft.show(fragment1).commit();
                }
                break;
            case 1:
                if (fragment1 != null) {ft.hide(fragment1);}
                if (fragment3 != null) {ft.hide(fragment3);}
                if (fragment4 != null) {ft.hide(fragment4);}
                if (fragment5 != null) {ft.hide(fragment5);}
                if (fragment6 != null) {ft.hide(fragment6);}
                if (fragment7 != null) {ft.hide(fragment7);}
                if (fragment2 == null) {
                    ft.commit();
                    fragment2 = new ProfileFragment();
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1
                            .add(R.id.content, fragment2, "");
                    ft1.addToBackStack(null).commit();
                } else {
                    ft.show(fragment2).commit();
                }
                break;
            case 2:
                if (fragment1 != null) {
                    ft.hide(fragment1);
                }
                if (fragment2 != null) {
                    ft.hide(fragment2);
                }
                if (fragment4 != null) {
                    ft.hide(fragment4);
                }
                if (fragment5 != null) {
                    ft.hide(fragment5);
                }
                if (fragment6 != null) {
                    ft.hide(fragment6);
                }
                if (fragment7 != null) {
                    ft.hide(fragment7);
                }
                if (fragment3 == null) {
                    ft.commit();
                    fragment3 = new UsersCardStackFragment();
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1
                            .add(R.id.content, fragment3, "");
                    ft1.addToBackStack(null).commit();                }
                else {
                    ft.show(fragment3).commit();
                }
                break;
            case 3:
                if (fragment1 != null) {
                    ft.hide(fragment1);
                }
                if (fragment2 != null) {
                    ft.hide(fragment2);
                }
                if (fragment3 != null) {
                    ft.hide(fragment3);
                }
                if (fragment5 != null) {
                    ft.hide(fragment5);
                }
                if (fragment6 != null) {
                    ft.hide(fragment6);
                }
                if (fragment7 != null) {
                    ft.hide(fragment7);
                }
                if (fragment4 == null) {
                    ft.commit();
                    fragment4 = new ChatListFragment();
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1
                            .add(R.id.content, fragment4, "");
                    ft1.addToBackStack(null).commit();
                }
                else {
                    ft.show(fragment4).commit();
                }
                break;
            case 4:
                if (fragment1 != null) {
                    ft.hide(fragment1);
                }
                if (fragment2 != null) {
                    ft.hide(fragment2);
                }
                if (fragment3 != null) {
                    ft.hide(fragment3);
                }
                if (fragment4 != null) {
                    ft.hide(fragment4);
                }
                if (fragment6 != null) {
                    ft.hide(fragment6);
                }
                if (fragment7 != null) {
                    ft.hide(fragment7);
                }
                if (fragment5 == null) {
                    ft.commit();
                    fragment5 = new NotificationsFragment();
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1
                            .add(R.id.content, fragment5, "");
                    ft1.addToBackStack(null).commit();                }
                else {
                    ft.show(fragment5).commit();
                }
                break;
            case 5:
                if (fragment1 != null) {
                    ft.hide(fragment1);
                }
                if (fragment2 != null) {
                    ft.hide(fragment2);
                }
                if (fragment3 != null) {
                    ft.hide(fragment3);
                }
                if (fragment5 != null) {
                    ft.hide(fragment5);
                }
                if (fragment4 != null) {
                    ft.hide(fragment4);
                }
                if (fragment7 != null) {
                    ft.hide(fragment7);
                }
                if (fragment6 == null) {
                    ft.commit();
                    fragment6 = new GroupChatsFragment();
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1
                            .add(R.id.content, fragment6, "");
                    ft1.addToBackStack(null).commit();                }
                else {
                    ft.show(fragment6).commit();
                }
                break;
            case 6:
                if (fragment1 != null) {
                    ft.hide(fragment1);
                }
                if (fragment2 != null) {
                    ft.hide(fragment2);
                }
                if (fragment3 != null) {
                    ft.hide(fragment3);
                }
                if (fragment5 != null) {
                    ft.hide(fragment5);
                }
                if (fragment6 != null) {
                    ft.hide(fragment6);
                }
                if (fragment4 != null) {
                    ft.hide(fragment4);
                }
                if (fragment7 == null) {
                    ft.commit();
                    fragment7 = new MapFragment();
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1
                            .add(R.id.content, fragment7, "");
                    ft1.addToBackStack(null).commit();                }
                else {
                    ft.show(fragment7).commit();
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        //check on start o app
        checkUserStatus();
        super.onStart();
    }

    @Override
    public void addLocationCallback(LocationCallback locationCallback) {
        Log.d(LOCATION_SERVICES, "addLocationCallback: ");
        mLocationCallback = locationCallback;

        //check if permissions not ok
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permission, 0);
        } else {
            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, null/*looper*/);

        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1:
                //call request location updates
                addLocationCallback(mLocationCallback);
                break;
        }
    }
    @Override
    public void removeLocationCallback(LocationCallback locationCallback) {
        Log.d(LOCATION_SERVICES, "removeLocationCallback: ");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

    }

    public void initLocation() {
        createLocationRequest();
        //location provider client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        initLocationSettings();

    }



    private void initLocationSettings() {
        //Builder for the location settings provider
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        //add a location request to the lsr
        builder.addLocationRequest(mLocationRequest);
        //we retrieve the client settings
        SettingsClient client = LocationServices.getSettingsClient(this);
        //we check the location settings with the builder
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, locationSettingsResponse -> Log.d(Constant.LOCATION_SERVICES, "initLocation: ok"));

        task.addOnFailureListener(this, e -> {
            Log.d(Constant.LOCATION_SERVICES, "initLocation: ko");
            // code from the google sample
            int statusCode = ((ApiException) e).getStatusCode();
            switch (statusCode) {
                case CommonStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(DashboardActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied. However, we have no way
                    // to fix the settings so we won't show the dialog.
                    break;
            }
        });
    }


    private void addFragments() {

        if (Objects.equals(fragPos, "0")) {
            //home fragment transaction
            toolbar.setTitle(getString(R.string.inicio)); //change actionbar title
            fragment1 = new PostsFragment();
            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
            ft1
                    .replace(R.id.content, fragment1, "");
            ft1.addToBackStack(null).commit();


            if (fragment2 != null) {
                FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                ft2.add(R.id.content, fragment2, "")
                        .hide(fragment2).commit();
            }

            if (fragment3 != null) {
                FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                ft3.add(R.id.content, fragment3, "")
                        .hide(fragment3).commit();
            }
            if (fragment4 != null) {
                FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                ft4.add(R.id.content, fragment4, "")
                        .hide(fragment4).commit();
            }
            if (fragment5 != null) {
                FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                ft5.add(R.id.content, fragment5, "")
                        .hide(fragment5).commit();
            }
            if (fragment6 != null) {
                FragmentTransaction ft6 = getSupportFragmentManager().beginTransaction();
                ft6.add(R.id.content, fragment6, "")
                        .hide(fragment6).commit();
            }
            if (fragment7 != null) {
                FragmentTransaction ft7 = getSupportFragmentManager().beginTransaction();
                ft7.add(R.id.content, fragment7, "")
                        .hide(fragment7).commit();
            }

        } else if (Objects.equals(fragPos, "1")) {
            //profile fragment transaction
            toolbar.setTitle(getString(R.string.perfil)); //change actionbar title
            fragment2 = new ProfileFragment();
            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
            ft1.replace(R.id.content, fragment2, "");
            ft1.addToBackStack(null).commit();


            if (fragment1 != null) {
                FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                ft2.add(R.id.content, fragment1, "")
                        .hide(fragment1).commit();
            }
            if (fragment3 != null) {
                FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                ft3.add(R.id.content, fragment3, "")
                        .hide(fragment3).commit();
            }
            if (fragment4 != null) {
                FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                ft4.add(R.id.content, fragment4, "")
                        .hide(fragment4).commit();
            }
            if (fragment5 != null) {
                FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                ft5.add(R.id.content, fragment5, "")
                        .hide(fragment5).commit();
            }
            if (fragment6 != null) {
                FragmentTransaction ft6 = getSupportFragmentManager().beginTransaction();
                ft6.add(R.id.content, fragment6, "")
                        .hide(fragment6).commit();
            }
            if (fragment7 != null) {
                FragmentTransaction ft7 = getSupportFragmentManager().beginTransaction();
                ft7.add(R.id.content, fragment7, "")
                        .hide(fragment7).commit();
            }


        } else if (Objects.equals(fragPos, "2")) {
            //users fragment transaction
            toolbar.setTitle(getString(R.string.users)); //change actionbar title


            fragment3 = new UsersCardStackFragment();
            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
            ft1.replace(R.id.content, fragment3, "");
            ft1.addToBackStack(null).commit();

            if (fragment1 != null) {
                FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                ft2.add(R.id.content, fragment1, "")
                        .hide(fragment1).commit();
            }
            if (fragment2 != null) {
                FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                ft3.add(R.id.content, fragment2, "")
                        .hide(fragment2).commit();
            }
            if (fragment4 != null) {
                FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                ft4.add(R.id.content, fragment4, "")
                        .hide(fragment4).commit();
            }
            if (fragment5 != null) {
                FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                ft5.add(R.id.content, fragment5, "")
                        .hide(fragment5).commit();
            }
            if (fragment6 != null) {
                FragmentTransaction ft6 = getSupportFragmentManager().beginTransaction();
                ft6.add(R.id.content, fragment6, "")
                        .hide(fragment6).commit();
            }
            if (fragment7 != null) {
                FragmentTransaction ft7 = getSupportFragmentManager().beginTransaction();
                ft7.add(R.id.content, fragment7, "")
                        .hide(fragment7).commit();
            }
        } else if (Objects.equals(fragPos, "3")) {
            //chats fragment transaction
            toolbar.setTitle(getString(R.string.chats)); //change actionbar title

            fragment4 = new ChatListFragment();
            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
            ft1.replace(R.id.content, fragment4, "");
            ft1.addToBackStack(null).commit();

            if (fragment1 != null) {
                FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                ft2.add(R.id.content, fragment1, "")
                        .hide(fragment1).commit();
            }
            if (fragment2 != null) {
                FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                ft3.add(R.id.content, fragment2, "")
                        .hide(fragment2).commit();
            }
            if (fragment3 != null) {
                FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                ft4.add(R.id.content, fragment3, "")
                        .hide(fragment3).commit();
            }
            if (fragment5 != null) {
                FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                ft5.add(R.id.content, fragment5, "")
                        .hide(fragment5).commit();
            }
            if (fragment6 != null) {
                FragmentTransaction ft6 = getSupportFragmentManager().beginTransaction();
                ft6.add(R.id.content, fragment6, "")
                        .hide(fragment6).commit();
            }
            if (fragment7 != null) {
                FragmentTransaction ft7 = getSupportFragmentManager().beginTransaction();
                ft7.add(R.id.content, fragment7, "")
                        .hide(fragment7).commit();
            }
        } else if (Objects.equals(fragPos, "4")) {
            //notification fragment transaction
            toolbar.setTitle(getString(R.string.notificaciones)); //change actionbar title
            fragment5 = new NotificationsFragment();

            if (!Objects.equals(extra, "")) {
                // Supply index input as an argument.
                Bundle args = new Bundle();
                args.putString("extra", extra);
                fragment5.setArguments(args);
            }


            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
            ft1.replace(R.id.content, fragment5, "");
            ft1.addToBackStack(null).commit();

            if (fragment4 != null) {
                //chats fragment transaction
                FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                ft4.add(R.id.content, fragment4, "");
                ft4.hide(fragment4).commit();
            }
            if (fragment3 != null) {
                FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                ft3.add(R.id.content, fragment3, "")
                        .hide(fragment3).commit();
            }
            if (fragment1 != null) {
                FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                ft5.add(R.id.content, fragment1, "")
                        .hide(fragment1).commit();
            }
            if (fragment2 != null) {
                FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                ft2.add(R.id.content, fragment2, "")
                        .hide(fragment2).commit();
            }
            if (fragment6 != null) {
                FragmentTransaction ft6 = getSupportFragmentManager().beginTransaction();
                ft6.add(R.id.content, fragment6, "")
                        .hide(fragment6).commit();
            }
            if (fragment7 != null) {
                FragmentTransaction ft7 = getSupportFragmentManager().beginTransaction();
                ft7.add(R.id.content, fragment7, "")
                        .hide(fragment7).commit();
            }

            navigationView.getMenu().getItem(4).setChecked(true);

        } else if (Objects.equals(fragPos, "5")) {
            //groups fragment transaction
            toolbar.setTitle(getString(R.string.grupos)); //change actionbar title

            fragment6 = new GroupChatsFragment();;
            FragmentTransaction ft6 = getSupportFragmentManager().beginTransaction();
            ft6.replace(R.id.content, fragment6, "");
            ft6.addToBackStack(null).commit();

            if (fragment4 != null) {
                //chats fragment transaction
                FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                ft4.add(R.id.content, fragment4, "");
                ft4.hide(fragment4).commit();
            }
            if (fragment1 != null) {
                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                ft1.add(R.id.content, fragment1, "")
                        .hide(fragment1).commit();
            }
            if (fragment2 != null) {
                FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                ft2.add(R.id.content, fragment2, "")
                        .hide(fragment2).commit();
            }
            if (fragment5 != null) {
                FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                ft5.add(R.id.content, fragment5, "")
                        .hide(fragment5).commit();
            }
            if (fragment7 != null) {
                FragmentTransaction ft7 = getSupportFragmentManager().beginTransaction();
                ft7.add(R.id.content, fragment7, "")
                        .hide(fragment7).commit();
            }
            if (fragment3 != null) {
                FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                ft3.add(R.id.content, fragment3, "")
                        .hide(fragment3).commit();
            }

            navigationView.getMenu().getItem(4).setChecked(true);


        } else if (Objects.equals(fragPos, "6")) {
            //maps fragment transaction
            toolbar.setTitle(getString(R.string.mapas)); //change actionbar title

            fragment7 = new MapFragment();;
            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
            ft1.replace(R.id.content, fragment7, "");
            ft1.addToBackStack(null).commit();

            if (fragment4 != null) {
                //chats fragment transaction
                FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                ft4.add(R.id.content, fragment4, "");
                ft4.hide(fragment4).commit();
            }
            if (fragment1 != null) {
                FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                ft2.add(R.id.content, fragment1, "")
                        .hide(fragment1).commit();
            }
            if (fragment2 != null) {
                FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                ft3.add(R.id.content, fragment2, "")
                        .hide(fragment2).commit();
            }
            if (fragment5 != null) {
                FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                ft5.add(R.id.content, fragment5, "")
                        .hide(fragment5).commit();
            }
            if (fragment6 != null) {
                FragmentTransaction ft6 = getSupportFragmentManager().beginTransaction();
                ft6.add(R.id.content, fragment6, "")
                        .hide(fragment6).commit();
            }
            navigationView.getMenu().getItem(4).setChecked(true);

        }

    }




    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3600000);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);//test 4g power balance
    }


}