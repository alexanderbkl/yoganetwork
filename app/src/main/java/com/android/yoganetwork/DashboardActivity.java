package com.android.yoganetwork;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.android.yoganetwork.constants.Constant;
import com.android.yoganetwork.fragments.ChatListFragment;
import com.android.yoganetwork.fragments.GroupChatsFragment;
import com.android.yoganetwork.fragments.HomeFragment;
import com.android.yoganetwork.fragments.MapFragment;
import com.android.yoganetwork.fragments.NotificationsFragment;
import com.android.yoganetwork.fragments.ProfileFragment;
import com.android.yoganetwork.fragments.UsersFragment;
import com.android.yoganetwork.notifications.Token;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.android.yoganetwork.constants.Constant.LOCATION_SERVICES;

import java.util.Objects;

public class DashboardActivity extends AppCompatActivity implements
        MapFragment.OnFragmentInteractionListener {



    public static final int INTERVAL = 60000;


    //firebase authentication
    FirebaseAuth firebaseAuth;
    Toolbar toolbar;

    String mUID;

    private    BottomNavigationView navigationView;

    private LocationCallback mLocationCallback;
    private final String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final int REQUEST_CHECK_SETTINGS = 1;

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
        getSupportActionBar().setTitle(R.string.inicio);

     //   toolbar.setVisibility(View.GONE);



        //home fragment transaction (default, on start)
        toolbar.setTitle(R.string.inicio); //change actionbar title
        Intent intent= getIntent();
        Bundle b = intent.getExtras();
/*
        if(b!=null)
        {
            int fragment = (int) b.get("fragment");
            if (fragment == 2) {
                ProfileFragment fragment2 = new ProfileFragment();
                FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                ft2.replace(R.id.content, fragment2, "");
                ft2.commit();

            }
        } else {*/
            HomeFragment fragment1 = new HomeFragment();
            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
            ft1.replace(R.id.content, fragment1, "");
            ft1.commit();

       /* }*/


        checkUserStatus();

        initLocation();


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

    private final BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    //handle item clicks
                  switch (item.getItemId()){
                      case R.id.nav_home:
                  //        toolbar.setVisibility(View.GONE);

                          //home fragment transaction
                          toolbar.setTitle("Inicio"); //change actionbar title
                          HomeFragment fragment1 = new HomeFragment();
                          FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                          ft1.replace(R.id.content, fragment1, "");
                          ft1.commit();

                          return true;
                      case R.id.nav_profile:
                          //profile fragment transaction
                   //       toolbar.setVisibility(View.GONE);

                          toolbar.setTitle(R.string.profile); //change actionbar title
                          ProfileFragment fragment2 = new ProfileFragment();
                          FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                          ft2.replace(R.id.content, fragment2, "");
                          ft2.commit();

                          return true;

                          case R.id.nav_users:
                          //users fragment transaction

                              toolbar.setTitle(R.string.users); //change actionbar title
                              UsersFragment fragment3 = new UsersFragment();
                              FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                              ft3.replace(R.id.content, fragment3, "");
                              ft3.commit();
                          return true;
                      case R.id.nav_chat:
                          //users fragment transaction
                      //    toolbar.setVisibility(View.GONE);

                          toolbar.setTitle("Chat"); //change actionbar title
                          ChatListFragment fragment4 = new ChatListFragment();
                          FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                          ft4.replace(R.id.content, fragment4, "");
                          ft4.commit();
                          return true;

                      case R.id.nav_more:
                        showMoreOptions();
                          return true;
                  }
                    return false;
                }
            };

    private void showMoreOptions() {
        //popup menu to show more options
        PopupMenu popupMenu = new PopupMenu(this, navigationView, Gravity.END);
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
                    //notification clicked

                    //notifications fragment transaction
         //           toolbar.setVisibility(View.GONE);

                    toolbar.setTitle(R.string.notificaciones); //change actionbar title
                    NotificationsFragment fragment5 = new NotificationsFragment();
                    FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                    ft5.replace(R.id.content, fragment5, "");
                    ft5.commit();
                }
                else if (id == 1) {
                    //group chats clicked

                    //group chats fragment transaction
               //     toolbar.setVisibility(View.GONE);

                    toolbar.setTitle(R.string.grupos); //change actionbar title
                    GroupChatsFragment fragment6 = new GroupChatsFragment();
                    FragmentTransaction ft6 = getSupportFragmentManager().beginTransaction();
                    ft6.replace(R.id.content, fragment6, "");
                    ft6.commit();
                }  else if (id == 2) {
                    //group chats clicked

                    //group chats fragment transaction
                    toolbar.setTitle("Maps"); //change actionbar title
                    MapFragment fragment7 = new MapFragment();
                    FragmentTransaction ft7 = getSupportFragmentManager().beginTransaction();
                    ft7.replace(R.id.content, fragment7, "");
                    ft7.commit();
            //        toolbar.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
        popupMenu.show();
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
            updateToken(MyFirebaseMessagingService.getToken(this));
        }
        else {
            //user not signed in, go to mainactivity
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permission, 0);
            }
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





    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//test 4g power balance
    }

}