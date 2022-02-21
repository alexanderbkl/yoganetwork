package com.android.yoganetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.android.yoganetwork.adapters.AdapterAddToGroup;
import com.android.yoganetwork.models.ModelGroupChatList;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AddToGroupActivity extends AppCompatActivity {

    RecyclerView groupsRv;
    FirebaseAuth firebaseAuth;
    private ArrayList<ModelGroupChatList> groupChatLists;
    private AdapterAddToGroup adapterAddToGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_group);


        groupsRv = findViewById(R.id.groupsRv);

        firebaseAuth = FirebaseAuth.getInstance();


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String groupId= extras.getString("groupId");
            String hisUid = extras.getString("hisUid");
            loadGroupChatsList(hisUid);
            System.out.println("suka"+groupId+hisUid);
            if (groupId != null && hisUid != null) {
                addParticipant(groupId, hisUid);
            }

        }


    }

        private void addParticipant(String groupId, String hisUid) {
        //setup user data - add user in group
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUid);
        hashMap.put("role", "participant");
        hashMap.put("timestamp", ""+timestamp);
        //add that user in Groups>groupId>Participants
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(hisUid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added successfully
                        Toast.makeText(AddToGroupActivity.this, "Añadido", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AddToGroupActivity.this, ThereProfileActivity.class);
                        intent.putExtra("uid",hisUid);
                        intent.putExtra("myUid", firebaseAuth.getUid());
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed adding user in group
                        Toast.makeText(AddToGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

      }

    private void loadGroupChatsList(String hisUid) {
        groupChatLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatLists.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //if current user's uid exists in participants list of group then show that group
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists()) {
                        ModelGroupChatList model = ds.getValue(ModelGroupChatList.class);
                        groupChatLists.add(model);
                    }
                }
                adapterAddToGroup = new AdapterAddToGroup(AddToGroupActivity.this, groupChatLists, hisUid);
                groupsRv.setAdapter(adapterAddToGroup);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}