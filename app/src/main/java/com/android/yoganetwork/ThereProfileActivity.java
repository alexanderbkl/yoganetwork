package com.android.yoganetwork;

import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ViewOutlineProvider;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.android.yoganetwork.adapters.AdapterPost;
import com.android.yoganetwork.adapters.AdapterUsers;
import com.android.yoganetwork.models.ModelPost;
import com.android.yoganetwork.notifications.Data;
import com.android.yoganetwork.notifications.Sender;
import com.android.yoganetwork.notifications.Token;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.text.TextUtils.isEmpty;

public class ThereProfileActivity extends AppCompatActivity {


    private FirebaseAuth firebaseAuth;
    //view from xml
    private ImageView avatarIv, coverIv;
    private TextView nameTv, realNameTv, typeTv, practicTv, dietTv, descriptionTv;

    private RecyclerView postsRecyclerView;

    private List<ModelPost> postList;
    private FloatingActionButton fab, likeFab;
    private AdapterPost adapterPosts;
    private String uid;
    private Toolbar toolbar;
    private String myUid;
    private boolean isLiked = false;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Perfil");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //init views
        avatarIv = findViewById(R.id.avatarIv);
        coverIv = findViewById(R.id.coverIv);
        nameTv = findViewById(R.id.nameTv);
        realNameTv = findViewById(R.id.realNameTv);
        fab = findViewById(R.id.fab);
        likeFab = findViewById(R.id.likeFab);
        typeTv = findViewById(R.id.typeTv);
        practicTv = findViewById(R.id.practicTv);
        dietTv = findViewById(R.id.dietTv);
        postsRecyclerView = findViewById(R.id.recycler_view);
        descriptionTv = findViewById(R.id.descriptionTv);

        firebaseAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(ThereProfileActivity.this);
        //get uid of clicked user to retrieve his posts
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        myUid = intent.getStringExtra("myUid");

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String pseudonymL = ""+ ds.child("pseudonym").getValue();
                    String pseudonym = pseudonymL;
                    if (!pseudonymL.equals("")) {
                        pseudonym = pseudonymL.substring(0, 1).toUpperCase() + pseudonymL.substring(1);
                    }
                    String realnameL = ""+ds.child("realname").getValue();
                    String realname = realnameL;
                    if (!realnameL.equals("")) {
                        realname = realnameL.substring(0, 1).toUpperCase() + realnameL.substring(1);
                    }
                    String typeL = ""+ ds.child("type").getValue();
                    String type = typeL;
                    if (!typeL.equals("")) {
                        type = typeL.substring(0, 1).toUpperCase() + typeL.substring(1);
                    }
                    String practicL = ""+ ds.child("practic").getValue();
                    String practic = practicL;
                    if (!practicL.equals("")) {
                        practic = practicL.substring(0, 1).toUpperCase() + practicL.substring(1);
                    }
                    String dietL = ""+ ds.child("diet").getValue();
                    String diet = dietL;
                    if (!dietL.equals("")) {
                        diet = dietL.substring(0, 1).toUpperCase() + dietL.substring(1);
                    }
                    String description = ""+ ds.child("description").getValue();

                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();



                    //set data
                    nameTv.setText(pseudonym);
                    realNameTv.setText(realname);
                    typeTv.setText(type);
                    practicTv.setText(practic);
                    dietTv.setText(diet);
                    descriptionTv.setText(description);
                    try {
                        //if image is received then set
                        Picasso.get().load(image).into(avatarIv);
                    } catch (Exception e) {
                        //if there is any exception while getting image then set default
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);
                    }
                    if (cover.isEmpty()) {
                        Picasso.get().load(R.drawable.ic_profile_black).fit().into(coverIv);
                    } else{
                        Picasso.get().load(cover).fit().into(coverIv);
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        postList = new ArrayList<>();

        checkUserStatus();
        checkIsLiked(uid);
        loadHistPosts();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imBlockedORNot(uid);
            }
        });

        likeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imBlockedORNot(uid);
                if (isLiked) {
                    isLiked = false;
                }
                else {
                    isLiked = true;
                    likeFab.setVisibility(View.INVISIBLE);
                    likeUser(uid);

                }
            }
        });
    }

    private void likeUser(String hisUid) {
        //like the user, by adding uid to current user's "LikedUsers" node
        //put values in hashmap to put in db
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUid);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("LikedUsers").child(hisUid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //get my pseudonym and picture url
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(myUid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String pseudonym = "" + snapshot.child("pseudonym").getValue();
                                String image = "" + snapshot.child("image").getValue();
                                //liked successfully
                                //send notification
                                sendNotification(
                                        ""+pseudonym+" te ha dado like!",
                                        uid,
                                        image
                                );
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });





                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to block
                        Toast.makeText(ThereProfileActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });




        //like the user, by adding uid to current user's "LikedUsers" node



        //add post id and uid in likes node
        DatabaseReference profileLikesRef = FirebaseDatabase.getInstance().getReference("Users").child(hisUid).child("profileLikes").child(myUid);
        profileLikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                snapshot.getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {


                        if (snapshot.exists()) {
                            //already liked, so remove like
                            profileLikesRef.child(myUid).removeValue();
                        }
                        else {

                            addToHisNotifications(hisUid, myUid);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void addToHisNotifications(String hisUid, String myUid) {
        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", "like");
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", "Liked your profile");
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("profileLikes").child(myUid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                    }
                });

    }

    private void sendNotification(String name, String hisUid, String hisPic) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(
                            ""+myUid,
                            ""+name,
                            "Someone liked you",
                            ""+hisUid,
                            "LikeNotification",
                            ""+hisPic
                    );
                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());

                    //fcm json object request
                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        //response of the request
                                        Log.d("JSON_RESPONSE", "onResponse: "+response.toString());
                                    }


                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onResponse: "+error.toString());

                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                //put params
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAAQamk8xY:APA91bHY2PqvH237jhVIXZEI0OlvUQACRVffSLfv_pU7gmO1EZL2wcV2J52AFpC3QL5H16DSsAUHwJ2T7nXiVAYgPGuMmyPXRs8efYuZlOWvIttxIl49GsrMw54939LA8gBFsXGp41S7");

                                return headers;
                            }
                        };
                        JsonObjectRequest jsonObjectRequest1 = new com.android.yoganetwork.utils.JsonObjectRequest().jsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj);


                        //add this request to queue
                        requestQueue.add(jsonObjectRequest1);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkIsLiked(String hisUid) {
        //check each user, if blocked or not
        //if uid of the user exists in "BlockedUsers then that user is blocked, otherwise not
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("LikedUsers").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                                isLiked = true;
                                likeFab.setVisibility(View.INVISIBLE);
                            } else {
                                isLiked = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void imBlockedORNot(String uid) {

        //first check if sender (current user) is blocked by receiver or not
        //Logic: if uid of the sender (current user) exists in "BlockedUsers" of receiver, then sender (current user) is blocked; otherwise not.
        //if blocked then just display a message i.e. "You're blocked by that user, can't send message
        //if not blocked then simply start the chat activity
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (ds.exists()) {
                                Toast.makeText(ThereProfileActivity.this, R.string.errormsg, Toast.LENGTH_SHORT).show();
                                //blocked, dont proceed further
                                return;
                            }
                        }
                        //not blocked, start activity
                        Intent intent = new Intent(ThereProfileActivity.this, ChattingActivity.class);
                        intent.putExtra("hisUid", uid);
                        ThereProfileActivity.this.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadHistPosts() {
        //linear layout for recyclervie
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        postsRecyclerView.setLayoutManager(layoutManager);
        //init posts list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query to load posts
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    //add to list
                    postList.add(myPosts);
                    //adapter
                    adapterPosts = new AdapterPost(ThereProfileActivity.this, postList, postsRecyclerView);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(ThereProfileActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void searchHistPosts(final String searchQuery) {
        //linear layout for recyclervie
        LinearLayoutManager layoutManager = new LinearLayoutManager(ThereProfileActivity.this);
        //show newest post first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        postsRecyclerView.setLayoutManager(layoutManager);
        //init posts list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query to load posts
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        //add to list
                        postList.add(myPosts);
                    }
                    //adapter
                    adapterPosts = new AdapterPost(ThereProfileActivity.this, postList, postsRecyclerView);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(ThereProfileActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
        } else {
            //user not signed in, go to mainactivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false); //hide add post from this activity
        menu.findItem(R.id.action_create_group).setVisible(false); //hide create group from this activity
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        MenuItem item = menu.findItem(R.id.action_search);
        //v7 searchview ot search user specific posts
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if (!isEmpty(s)) {
                    //search
                    searchHistPosts(s);
                } else {
                    loadHistPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!isEmpty(s)) {
                    //search
                    searchHistPosts(s);
                } else {
                    loadHistPosts();
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();

        }
        if (id == R.id.action_add_participant) {

            Intent i = new Intent(ThereProfileActivity.this, AddToGroupActivity.class);
            i.putExtra("hisUid", uid);
            startActivity(i);


        }
        return super.onOptionsItemSelected(item);
    }




}