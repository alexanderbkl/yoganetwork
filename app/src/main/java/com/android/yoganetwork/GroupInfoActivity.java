package com.android.yoganetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.yoganetwork.adapters.AdapterParticipantAdd;
import com.android.yoganetwork.models.ModelUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class GroupInfoActivity extends AppCompatActivity {

    private String groupId;
    private String myGroupRole = "";

    private FirebaseAuth firebaseAuth;

    private Toolbar toolbar;

    //ui views
    private ImageView groupIconIv;
    private TextView descriptionTv, createdByTv, editGroupTv, addParticipantTv, leaveGroupTv, participantsTv;
    private RecyclerView participantsRv;

    private ArrayList<ModelUsers> usersList;
    private AdapterParticipantAdd adapterParticipantAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_group_activity);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        groupIconIv = findViewById(R.id.groupIconIv);
        descriptionTv = findViewById(R.id.descriptionTv);
        createdByTv = findViewById(R.id.createdByTv);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
        editGroupTv = findViewById(R.id.editGroupTv);
        addParticipantTv = findViewById(R.id.addParticipantTv);
        participantsTv = findViewById(R.id.participantsTv);
        participantsRv = findViewById(R.id.participantsRv);


        groupId = getIntent().getStringExtra("groupId");

        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadMyGroupRole();

        addParticipantTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupParticipantAddActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        editGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupEditActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        leaveGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if user is participant/admin: leave group
                //if user is creator: delete group
                String dialogTitle="";
                String dialogDescription="";
                String positiveButtonTitle="";
                if (myGroupRole.equals("creator")) {
                    dialogTitle=getString(R.string.eliminargrp);
                    dialogDescription=getString(R.string.eliminargrpseguro);
                    positiveButtonTitle =getString(R.string.eliminar1);
                }
                else {
                    dialogTitle =getString(R.string.salirgrp);
                    dialogDescription = getString(R.string.seguroeliminargrp);
                    positiveButtonTitle =getString(R.string.salir);
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (myGroupRole.equals("creator")){
                                    //im creator of group: delete group
                                deleteGroup();
                                }
                                else {
                                    //im participant/admin: leave group
                                leaveGroup();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancelar1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

    }

    private void leaveGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(firebaseAuth.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //group left sucessfully
                        Toast.makeText(GroupInfoActivity.this, R.string.salistedelgrp, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(GroupInfoActivity.this, DashboardActivity.class));
                    finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to leave group
                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void deleteGroup() {
     DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
     ref.child(groupId)
             .removeValue()
             .addOnSuccessListener(new OnSuccessListener<Void>() {
                 @Override
                 public void onSuccess(Void aVoid) {
                     //sucessfully deleted group
                     Toast.makeText(GroupInfoActivity.this, R.string.grpeliminado, Toast.LENGTH_SHORT).show();
                     startActivity(new Intent(GroupInfoActivity.this, DashboardActivity.class));
                     finish();
                 }
             })
             .addOnFailureListener(new OnFailureListener() {
                 @Override
                 public void onFailure(@NonNull Exception e) {
                //failed deleting group
                     Toast.makeText(GroupInfoActivity.this, "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                 }
             });
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               for (DataSnapshot ds: snapshot.getChildren()) {
                   //get group info
                   String groupId = ""+ds.child("groupId").getValue();
                   String groupTitle = ""+ds.child("groupTitle").getValue();
                   String groupDescription = ""+ds.child("groupDescription").getValue();
                   String groupIcon = ""+ds.child("groupIcon").getValue();
                   String createdBy = ""+ds.child("createdBy").getValue();
                   String timestamp = ""+ds.child("timestamp").getValue();

                   //convert timestamp to dd/mm/yyyy hh:mm am/pm
                   Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                   cal.setTimeInMillis(Long.parseLong(timestamp));
                   String dateTime = DateFormat.format("dd/MM/yyyy HH:mm", cal).toString();

                   loadCreatorInfo(dateTime, createdBy);

                   //set group info
                   toolbar.setTitle(groupTitle);
                   descriptionTv.setText(groupDescription);

                  try {
                      Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_primary).into(groupIconIv);
                  } catch (Exception e) {
                      groupIconIv.setImageResource(R.drawable.ic_group_primary);
                  }
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCreatorInfo(String dateTime, String createBy) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(createBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot ds: snapshot.getChildren()) {
                String pseudonym = ""+ds.child("pseudonym").getValue();
                createdByTv.setText(getString(R.string.creadopor)+pseudonym+getString(R.string.el)+dateTime);
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").orderByChild("uid")
                .equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                   for (DataSnapshot ds: snapshot.getChildren()){
                       myGroupRole = ""+ds.child("role").getValue();
                       toolbar.setSubtitle(""+myGroupRole);

                       if (myGroupRole.equals("participant")){
                       editGroupTv.setVisibility(View.GONE);
                       addParticipantTv.setVisibility(View.GONE);
                       leaveGroupTv.setText(R.string.salirgrp);
                       }
                       else if (myGroupRole.equals("admin")) {
                            editGroupTv.setVisibility(View.GONE);
                            addParticipantTv.setVisibility(View.VISIBLE);
                            leaveGroupTv.setText(R.string.salirgrp);
                       }
                       else if (myGroupRole.equals("creator")) {
                           editGroupTv.setVisibility(View.VISIBLE);
                           addParticipantTv.setVisibility(View.VISIBLE);
                           leaveGroupTv.setText(R.string.eliminargrp);
                       }
                   }
                        loadParticipants();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadParticipants() {
        usersList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
              for (DataSnapshot ds: snapshot.getChildren()) {
                  //get uid from Group > Participants
                  String uid = ""+ds.child("uid").getValue();

                  //get info of user using uid we got above
                  DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                  ref.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot snapshot) {
                          for (DataSnapshot ds: snapshot.getChildren()) {
                              ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                              usersList.add(modelUsers);
                          }
                          //adapter
                          adapterParticipantAdd = new AdapterParticipantAdd(GroupInfoActivity.this, usersList, groupId, myGroupRole);
                          //set adapter
                          participantsRv.setAdapter(adapterParticipantAdd);
                          participantsTv.setText(getString(R.string.participantes)+usersList.size()+")");
                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError error) {

                      }
                  });
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}