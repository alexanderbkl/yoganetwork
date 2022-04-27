package com.android.yoganetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.android.yoganetwork.adapters.AdapterUsers;
import com.android.yoganetwork.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CommentLikedByActivity extends AppCompatActivity {

    String cid;

    private RecyclerView recyclerView;
    private List<ModelUsers> usersList;
    private AdapterUsers adapterUsers;
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_liked_by);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //actionbar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Likes");
        //add back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();


        recyclerView = findViewById(R.id.recyclerView);

        //get the post id
        Intent intent = getIntent();
        cid = intent.getStringExtra("cid");

        usersList = new ArrayList<>();

        //get the list of UIDs of users who liked t he post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Likes").child("CommentLikes");
        ref.child(cid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    String hisUid = ""+ ds.getRef().getKey();

                    //get user info from each id
                    getUsers(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUsers(String hisUid) {
        //get information of each user using uid
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                            usersList.add(modelUsers);
                        }
                        //setup adapters
                        adapterUsers = new AdapterUsers(CommentLikedByActivity.this, usersList);
                        //set adapter to recyclerview
                        recyclerView.setAdapter(adapterUsers);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go to previous activity
        return super.onSupportNavigateUp();
    }
}