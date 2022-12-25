package com.amit.yoganet.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amit.yoganet.ChattingActivity;
import com.amit.yoganet.ThereProfileActivity;
import com.amit.yoganet.models.ModelUsers;
import com.amit.yoganet.R;
import com.amit.yoganet.notifications.Data;
import com.amit.yoganet.notifications.Sender;
import com.amit.yoganet.notifications.Token;
import com.amit.yoganet.utils.MapUtils;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.amit.yoganet.constants.Database.userLocation;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>
{
    private final Context context;
    List<ModelUsers> userList;


    private final String myUid;
    private RequestQueue requestQueue;
    boolean liked;

    //constructor


    public AdapterUsers(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.userList = usersList;

        //for getting current user's uid
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout (row_user.xml)
        //!!!
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup, false);


        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, @SuppressLint("RecyclerView") int i) {

        liked = false;

        requestQueue = Volley.newRequestQueue(context);



        //get data
        String hisUid = userList.get(i).getUid();
        String userImage = userList.get(i).getImage();
        String userNameL = userList.get(i).getPseudonym();
        String userName = userNameL;
        if (!userNameL.equals("")) {
            userName = userNameL.substring(0,1).toUpperCase() + userNameL.substring(1);
        }
       String userPracticL = userList.get(i).getPractic();
       String userPractic = userPracticL;
       if (!userPracticL.equals("")) {
           userPractic = userPracticL.substring(0,1).toUpperCase() + userPracticL.substring(1);
       }
        String userTypeL = userList.get(i).getType();
       String userType = userTypeL;
       if (!userTypeL.equals("")) {
           userType = userTypeL.substring(0,1).toUpperCase() + userTypeL.substring(1);
       }
       String userDescription = userList.get(i).getDescription();
    //set data
        myHolder.mNameTv.setText(userName);
        myHolder.mPracticTv.setText(userPractic);
        myHolder.mTypeTv.setText(userType);
        myHolder.mDescriptionTv.setText(userDescription);
        try {
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_default_img_white)
                    .into(myHolder.mAvatarIv);
        }
catch (Exception e) {

}
        //check if each user is blocked or not
        checkIsBlocked(hisUid, myHolder, i);
        checkIsLiked(hisUid, myHolder, i);



        //create a database reference to userLocations
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(userLocation);
        //create a datasnapshot to get the key name of every child inside userLocation
        ref.addValueEventListener(new ValueEventListener() {

            String myGeoHash="";
            String hisGeoHash="";

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //get the key of every child inside userLocation
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get the key of every child inside userLocation
                    String key = ds.getKey();
                    //get the value of every child inside userLocation
                    String value = ds.child("g").getValue().toString();
                    //check if the key is equal to the current user's uid
                    if (key.equals(myUid)) {
                        //if the key is equal to the current user's uid, then get the value of the child
                        //and set it to the current user's location
                        myGeoHash  = value;

                    } else if (key.equals(hisUid)) {
                        //if the key is equal to the other user's uid, then get the value of the child
                        //and set it to the other user's location
                        hisGeoHash = value;
                    }

                    if (!myGeoHash.equals("") && !hisGeoHash.equals("")) {
                        double[] myLocation = MapUtils.decodeHash(myGeoHash);
                        double[] hisLocation = MapUtils.decodeHash(hisGeoHash);
                        double myLat = myLocation[0];
                        double myLon = myLocation[1];
                        double hisLat = hisLocation[0];
                        double hisLon = hisLocation[1];

                        //calculate the distance between the current user and the other user
                        double distanceRaw = MapUtils.distance(myLat, myLon, hisLat, hisLon);
                        BigDecimal distance = BigDecimal.valueOf(distanceRaw);
                        distance = distance.setScale(2, RoundingMode.HALF_UP);

                        //set the distance to the textview
                        myHolder.mDistanceTv.setText(distance + " km");


                    }





                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });


        //handle item click
        myHolder.itemView.setOnClickListener(v -> {
            //show dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setItems(R.array.users_array, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        //profile clicked
                        //click to go to ThereProfileActivity with uid, this uid is of clicked user
                        //which will be used to show user specifi data/posts
                        Intent intent = new Intent(context, ThereProfileActivity.class);
                        intent.putExtra("uid", hisUid);
                        intent.putExtra("myUid", myUid);
                        context.startActivity(intent);
                    }
                    if (which ==1 ) {
                        //chat clicked
                        /*Click use from user list to start chatting/messaging
                         * Start activity by putting UID of receiver
                         * we will use that UID to identify the user we are gonna chat*/
                       imBlockedORNot(hisUid);
                    }
                }
            });
builder.create().show();
        });
        //click to block unblock user
        myHolder.blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (userList.get(i).isBlocked()) {
                unBlockUser(hisUid);
            }
            else {
                blockUser(hisUid);
            }
            }
        });

        myHolder.likeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userList.get(i).isLiked()) {
                    myHolder.likeIv.setImageResource(R.drawable.ic_heart_dark);
                    unLikeUser(hisUid, myHolder, i);
            }
            else {
                if (!liked) {
                    myHolder.likeIv.setImageResource(R.drawable.ic_heart_dark);
                    likeUser(hisUid, myHolder, i);
                }

            }
            }
        });
    }

    private void imBlockedORNot (String hisUid) {
        //first check if sender (current user) is blocked by receiver or not
        //Logic: if uid of the sender (current user) exists in "BlockedUsers" of receiver, then sender (current user) is blocked; otherwise not.
        //if blocked then just display a message i.e. "You're blocked by that user, can't send message
        //if not blocked then simply start the chat activity
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                                Toast.makeText(context, context.getString(R.string.nomensaje), Toast.LENGTH_SHORT).show();
                                //blocked, dont proceed further
                                return;
                            }
                        }
                        //not blocked, start activity
                        Intent intent = new Intent(context, ChattingActivity.class);
                        intent.putExtra("hisUid", hisUid);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsBlocked(String hisUid, MyHolder myHolder, int i) {
        //check each user, if blocked or not
        //if uid of the user exists in "BlockedUsers then that user is blocked, otherwise not
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                             //   myHolder.blockIv.setImageResource(R.drawable.ic_heart_red);
                                userList.get(i).setBlocked(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsLiked(String hisUid, MyHolder myHolder, int i) {
        //check each user, if blocked or not
        //if uid of the user exists in "BlockedUsers then that user is blocked, otherwise not
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("LikedUsers").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                                myHolder.likeIv.setImageResource(R.drawable.ic_heart_red);
                                userList.get(i).setLiked(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser(String hisUid) {
        //block the user, by adding uid to current user's "BlockedUsers" node

        //put values in hashmap to put in db
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUid);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    //blocked successfully
                        Toast.makeText(context, context.getString(R.string.bloqueado), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    //failed to block
                        Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void likeUser(String hisUid, MyHolder myHolder, int i) {

        liked = true;

        //like the user, by adding uid to current user's "LikedUsers" node

        //put values in hashmap to put in db
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUid);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("LikedUsers").child(hisUid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    //liked successfully
                        userList.get(i).setLiked(true);
                        String hisName = userList.get(i).getPseudonym();
                        String hisPic = userList.get(i).getImage();
                        //send notification
                        sendNotification(
                                ""+hisName+" te ha dado like!",
                                hisUid,
                                hisPic
                        );


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    //failed to block
                        Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void unBlockUser(String hisUid) {
        //unblock the user, by removing uid from current user's "BlockedUsers" node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                                //remove blocked user data from current user's BlockedUsers list
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //unblocked successfully
                                                Toast.makeText(context, context.getString(R.string.desbloqueado), Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed to unblock
                                                Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

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
                        Toast.makeText(context, "Notificationt"+senderJsonObj.toString(), Toast.LENGTH_SHORT).show();
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
                        JsonObjectRequest jsonObjectRequest1 = new com.amit.yoganet.utils.JsonObjectRequest().jsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj);


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

    private void sendLikeNotification(JSONObject notificationJo) {
        //send volley object request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("FCM_RESPONSE", "onResponse: "+response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //error occurred
                        Toast.makeText(context, "err"+error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=AAAAQamk8xY:APA91bHY2PqvH237jhVIXZEI0OlvUQACRVffSLfv_pU7gmO1EZL2wcV2J52AFpC3QL5H16DSsAUHwJ2T7nXiVAYgPGuMmyPXRs8efYuZlOWvIttxIl49GsrMw54939LA8gBFsXGp41S7");
                return headers;
            }
        };
        //enqueue the volley request
        Volley.newRequestQueue(context).add(jsonObjectRequest);
    }

    private void unLikeUser(String hisUid, MyHolder myHolder, int i) {
        //unblock the user, by removing uid from current user's "BlockedUsers" node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("LikedUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                                //remove blocked user data from current user's BlockedUsers list
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //unliked successfully
                                                userList.get(i).setLiked(false);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed to unblock
                                                Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    /*In this Part(12):
* ->Show sent messages
*   -Design different layouts for sender and receiver
*   -I'll use custom background for sender and receiver you can download using
* link in description*/
    @Override
    public int getItemCount() {

        return userList.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        ImageView mAvatarIv, blockIv, likeIv;
        TextView mNameTv, mPracticTv, mTypeTv, mDescriptionTv, mDistanceTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            blockIv = itemView.findViewById(R.id.blockIv);
            likeIv = itemView.findViewById(R.id.likeIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            mPracticTv = itemView.findViewById(R.id.practicTv);
            mTypeTv = itemView.findViewById(R.id.typeTv);
            mDescriptionTv = itemView.findViewById(R.id.descriptionTv);
            mDistanceTv = itemView.findViewById(R.id.distanceTv);


        }
    }
}
