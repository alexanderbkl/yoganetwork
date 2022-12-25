package com.amit.yoganet;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.BitmapCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.amit.yoganet.ChatView.AttachmentOption;
import com.amit.yoganet.ChatView.AttachmentOptionsListener;
import com.amit.yoganet.ChatView.AudioRecordView;
import com.amit.yoganet.adapters.AdapterChat;
import com.amit.yoganet.crop.CropImage;
import com.amit.yoganet.models.ModelChat;
import com.amit.yoganet.models.ModelUsers;
import com.amit.yoganet.notifications.Data;
import com.amit.yoganet.notifications.Sender;
import com.amit.yoganet.notifications.Token;
import com.amit.yoganet.utils.ImageUtils;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.amit.yoganet.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public class ChattingActivity extends AppCompatActivity implements AudioRecordView.RecordingListener, View.OnClickListener, AttachmentOptionsListener {

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 103;
    private static final int PICK_AUD_REQUEST = 104;
    private AudioRecordView audioRecordView;
    private RecyclerView recyclerViewMessages;
    private AdapterChat messageAdapter;
    private ArrayList<ModelChat> chatList;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private boolean isBlocked = false;
    private boolean isLiked = false;
    private RequestQueue requestQueue;
    private ValueEventListener seenListener;
    private DatabaseReference userRefForSeen;
    private DatabaseReference readMessagesRef;
    private DatabaseReference sendMessageRef;
    private DatabaseReference chatListRef1;
    private DatabaseReference chatListRef2;
    private DatabaseReference sendImageMessageRef;
    private DatabaseReference usersDbRef;
    private String hisImage;
    private String hisUid;
    private String myUid;
    private String chatRoomId;
    private ImageView profileIv, blockIv, likeIv;
    private TextView nameTv, userStatusTv;
    private boolean notify = false;
    private MediaRecorder recorder;
    private String audioFileName, audioFilePath;
    private Uri audioPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.amit.yoganet.R.layout.activity_chatting);

        firebaseDatabase = FirebaseDatabase.getInstance();
        audioRecordView = new AudioRecordView();
        audioRecordView.initView(findViewById(com.amit.yoganet.R.id.layoutMain));
        View containerView = audioRecordView.setContainerView(com.amit.yoganet.R.layout.layout_chatting);
        audioRecordView.setRecordingListener(this);
        recyclerViewMessages = containerView.findViewById(com.amit.yoganet.R.id.chat_recyclerView);
        Toolbar toolbar = findViewById(com.amit.yoganet.R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        userStatusTv = findViewById(com.amit.yoganet.R.id.userStatusTv);
        nameTv = findViewById(com.amit.yoganet.R.id.nameTv);
        profileIv = findViewById(com.amit.yoganet.R.id.profileIv);
        blockIv = findViewById(com.amit.yoganet.R.id.blockIv);
        likeIv = findViewById(com.amit.yoganet.R.id.likeIv);

        firebaseAuth = FirebaseAuth.getInstance();

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        //search user to get that user's info
        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");
        myUid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        char[] myCharUid = myUid.toCharArray(); for (char ch : myCharUid)
        { sb1.append((byte) ch);
        }
        char[] hisCharUid = hisUid.toCharArray(); for (char ch : hisCharUid)
        { sb2.append((byte) ch);
        }

        String myStringUid = String.valueOf(sb1);
        String hisStringUid = String.valueOf(sb2);

        BigInteger myBigUid = new BigInteger(myStringUid);
        BigInteger hisBigUid = new BigInteger(hisStringUid);

        chatRoomId = String.valueOf(myBigUid.add(hisBigUid));
        readMessagesRef = firebaseDatabase.getReference("ChatRooms").child(chatRoomId);
        sendMessageRef = firebaseDatabase.getReference("Users").child(myUid);
        chatListRef1 = firebaseDatabase.getReference("Chatlist").child(myUid).child(hisUid);
        chatListRef2 = firebaseDatabase.getReference("Chatlist").child(hisUid).child(myUid);
        sendImageMessageRef = firebaseDatabase.getReference("Chatlist").child(hisUid).child(myUid);
        usersDbRef = firebaseDatabase.getReference("Users");


        audioRecordView.getMessageView().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() ==0) {
                    checkTypingStatus("noOne");
                }
                else {
                    checkTypingStatus(hisUid); //uid of receiver
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });



        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        } else {
            readMessages();
        }




        checkIsBlocked();
        seenMessage();

        //handle item click
        toolbar.setOnClickListener(v -> {

            //profile clicked
            //click to go to ThereProfileActivity with uid, this uid is of clicked user
            //which will be used to show user specific data/posts
            Intent intent2 = new Intent(ChattingActivity.this, ThereProfileActivity.class);
            intent2.putExtra("uid", hisUid);
            startActivity(intent2);
        });


        Query userQuery = usersDbRef.orderByChild("uid").equalTo(hisUid);
        //get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required info is received
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    String name =""+ ds.child("pseudonym").getValue();
                    hisImage =""+ ds.child("image").getValue();
                    String typingStatus =""+ ds.child("typingTo").getValue();
                    //check typing status

                    if (typingStatus.equals(myUid)) {
                        userStatusTv.setText(com.amit.yoganet.R.string.escribiendo);
                    }
                    else {
                        //get value of online status
                        String onlineStatus = ""+ ds.child("onlineStatus").getValue();
                        if (onlineStatus.equals("Online")) {
                            userStatusTv.setText(onlineStatus);
                        }
                        else {
                            //convert timestamp to proper time date
                            //convert timestamp to dd/mm/yyyy hh:mm am/pm
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy HH:mm", cal).toString();
                            userStatusTv.setText(getString(com.amit.yoganet.R.string.ultvez)+dateTime);
                            notify = true;
                        }
                    }

                    //set data
                    nameTv.setText(name);
                    try {
                        //image received, set it to imageview in toolbar
                        Glide.with(getApplication().getApplicationContext()).load(hisImage).fitCenter().placeholder(com.amit.yoganet.R.drawable.ic_default_img_white).into(profileIv);
                    }
                    catch (Exception e) {
                        //there is exception getting picture, set default picture
                        Glide.with(getApplication().getApplicationContext()).load(com.amit.yoganet.R.drawable.ic_default_img_white).fitCenter().placeholder(com.amit.yoganet.R.drawable.ic_default_img_white).into(profileIv);
                        Toast.makeText(ChattingActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(linearLayoutManager);
        recyclerViewMessages.setHasFixedSize(false);

        messageAdapter = new AdapterChat(ChattingActivity.this, chatList, hisImage);

        recyclerViewMessages.setAdapter(messageAdapter);
        recyclerViewMessages.setItemAnimator(new DefaultItemAnimator());
        //set max recycled views
        setListener();

        //click to block unblock user
/*       blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBlocked) {
                    unBlockUser();
                }
                else {
                    blockUser();
                }
            }
        }); */


        //click to like unlike user
  /*     likeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLiked) {
                    unLikeUser();
                }
                else {
                    likeUser();
                }
            }
        });*/

        audioRecordView.getMessageView().requestFocus();

        containerView.findViewById(com.amit.yoganet.R.id.profileIv).setOnClickListener(this);


        audioRecordView.setAttachmentOptions(AttachmentOption.getList(), this);

        audioRecordView.removeAttachmentOptionAnimation(false);

    }

    private void checkIsBlocked() {
        //check each user, if blocked or not
        //if uid of the user exists in "BlockedUsers then that user is blocked, otherwise not
        DatabaseReference ref = firebaseDatabase.getReference("Users");
        ref.child(firebaseAuth.getCurrentUser().getUid()).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                                //         blockIv.setImageResource(R.drawable.ic_heart_red);
                                isBlocked = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void seenMessage() {
        userRefForSeen = firebaseDatabase.getReference("ChatRooms");
        seenListener = userRefForSeen.child(chatRoomId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)) {
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkTypingStatus(String typing) {
        DatabaseReference dbRef = firebaseDatabase.getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        //update value of onlineStatus of current user
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        //set online
        checkOnlineStatus("Online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //get timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        //set offline with last seen timestamp
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListener);

    }

    @Override
    protected void onResume() {
        //set online
        checkOnlineStatus("Online");
        super.onResume();
    }


    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = firebaseDatabase.getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //update value of onlineStatus of current user
        dbRef.updateChildren(hashMap);
    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
            myUid = user.getUid(); //currently signed in user's uid
        }
        else {
            //user not signed in, go to mainactivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }


    private void setListener() {

        audioRecordView.getEmojiView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordView.hideAttachmentOptionView();
            }
        });

        audioRecordView.getCameraView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordView.hideAttachmentOptionView();
                showImagePickDialog();

            }
        });

        audioRecordView.getSendView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = audioRecordView.getMessageView().getText().toString().trim();
                audioRecordView.hideAttachmentOptionView();
                //get text from edit text
                //check if text is empty or not
                if (TextUtils.isEmpty(message)) {
                    //text empty
                    Toast.makeText(ChattingActivity.this, com.amit.yoganet.R.string.nosepuedeenviarmsg, Toast.LENGTH_SHORT).show();
                }
                else {
                    //text not empty
                    sendMessage(message);
                }
                //reset edittext after sending message
                audioRecordView.getMessageView().setText("");

            }
        });





    }

    private void showImagePickDialog() {
        CropImage.activity()
                .start(this);

    }

    private void sendMessage(String message) {
        /*"Chats" node will be created that will contain all chats
         * Whenever user sends message it will create new child in "Chats" node and that child will contain
         * the following key values
         * sender: UID of sender
         * receiver: UID of receiver
         * message: the actual message*/

        DatabaseReference chatRooms = firebaseDatabase.getReference("ChatRooms").child(chatRoomId);

        String timestamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("timestamp", timestamp);
        hashMap.put("message", message);
        hashMap.put("isSeen", false);
        hashMap.put("type", "text");
        chatRooms.child(timestamp).setValue(hashMap);





        sendMessageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUsers user = snapshot.getValue(ModelUsers.class);
                String pseudonym = user.getPseudonym();
                if (pseudonym == null) {
                    pseudonym = "user";
                }
                if (notify) {
                    senNotification(hisUid, pseudonym, message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //create chatlist node/child in firebase database;
        chatListRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatListRef1.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ;
        chatListRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatListRef2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readMessages() {
        chatList = new ArrayList<>();

        readMessagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();

                for (DataSnapshot ds: snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    chatList.add(chat);
                    //adapter
                    messageAdapter.setChatList(chatList);
                    messageAdapter.notifyItemInserted(chatList.size() - 1);                    //set adapter to recycleview
                    recyclerViewMessages.setAdapter(messageAdapter);

                    recyclerViewMessages.smoothScrollToPosition(recyclerViewMessages.getAdapter().getItemCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onRecordingStarted() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO },
                    10);
        } else {
            //start recording audio
            recordAudio();
        }

    }

    private void recordAudio() {
        //create file to record audio
        audioFileName = UUID.randomUUID().toString() + ".3gp";
        audioFilePath = ChattingActivity.this.getExternalFilesDir(null).getAbsolutePath() + "/" + audioFileName;
        //create media recorder
        recorder = new MediaRecorder();
        //set audio source
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //set output format
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //set audio encoder
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        //set output file
        recorder.setOutputFile(audioFilePath);
        //try to start recording
        try {
            recorder.prepare();
            recorder.start();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error"+e.getMessage());
        }


    }

    @Override
    public void onRecordingLocked() {

    }

    @Override
    public void onRecordingCompleted() {
        //stop recording audio and upload to firebase
        completedRecording();
    }

    private void completedRecording() {
        if (recorder != null) {
            //stop recording audio
            try {
                recorder.stop();
                recorder.release();
                recorder = null;
                //upload audio to firebase
                uploadAudio();
            } catch(RuntimeException stopException) {
                // handle cleanup here
            }

        }

    }

    private void uploadAudio() {
        //create storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String timestamp = String.valueOf(System.currentTimeMillis());
        //create file reference
        Uri audioFile = null;
        if (audioFilePath != null) {
            //create file reference
            audioFile = Uri.fromFile(new File(audioFilePath));
        } else if (audioPath != null) {
            audioFile = audioPath;
        }

        final StorageReference fileReference = storageReference.child("ChatAudios/" + timestamp + ".3gp");
        //upload file
        assert audioFile != null;
        fileReference.putFile(audioFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //get url of audio
                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        //send audio url to firebase
                        String url = uri.toString();
                        //add image uri and other info to database
                        DatabaseReference chatRooms = firebaseDatabase.getReference("ChatRooms").child(chatRoomId);

                        //setup required data
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender", myUid);
                        hashMap.put("receiver", hisUid);
                        hashMap.put("message", url);
                        hashMap.put("timestamp", timestamp);
                        hashMap.put("type", "audio");
                        hashMap.put("isSeen", false);
                        //put this data to firebase
                        chatRooms.child(timestamp).setValue(hashMap);

                        //send notification
                        sendMessageRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ModelUsers user = snapshot.getValue(ModelUsers.class);
                                String pseudonym = user.getPseudonym();
                                if (pseudonym == null) {
                                    pseudonym = "user";
                                }
                                if (notify) {
                                    senNotification(
                                            hisUid, pseudonym,
                                            "sent you an audio message");
                                }
                                notify = false;
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                        //create chatlist node/child in firebase database
                        chatListRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    chatListRef1.child("id").setValue(hisUid);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                        chatListRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    chatListRef2.child("id").setValue(myUid);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });



                        audioFilePath = null;
                        audioPath = null;
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChattingActivity.this, "Failed to upload audio", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRecordingCanceled() {
        //cancel recording audio
        cancelRecording();
    }

    private void cancelRecording() {
        //stop recording audio
        recorder.stop();
        recorder.release();
        recorder = null;
        //delete audio file
        File file = new File(audioFilePath);
        file.delete();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onClick(AttachmentOption attachmentOption) {
        switch (attachmentOption.getId()) {
            case AttachmentOption.GALLERY_ID:
                showImagePickDialog();
                break;
            case AttachmentOption.AUDIO_ID:
                chooseAud();
                break;
        }
    }

    private void chooseAud() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Aud"), PICK_AUD_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK) {

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);


                assert result != null;
                Uri image_rui = result.getUri();
                try {
                    sendImageMessage(image_rui);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (requestCode == PICK_AUD_REQUEST && data != null && data.getData() != null) {
                //Toast.makeText(this, "Got data", Toast.LENGTH_SHORT).show();
                audioPath = data.getData();

                uploadAudio();

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendImageMessage(Uri image_rui) throws IOException {

        //progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(com.amit.yoganet.R.string.enviandoimg));
        progressDialog.show();
        String timeStamp = ""+System.currentTimeMillis();

        String fileNameAndPath = "ChatImages/" +"post_"+timeStamp;


        int bitSize = 4000000;
        int quality = 70;



        //get image from imageview
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] data = baos.toByteArray();
        Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_rui);
        Bitmap bitmap2;
        int n = 1;
        do {
            bitmap2 = new ImageUtils().getResizedBitmap(bitmap1,bitmap1.getWidth()/n,bitmap1.getHeight()/n);
            if (n % 2 != 0) {
                n++;
            } else {
                n+=2;
            }
        } while (BitmapCompat.getAllocationByteCount(bitmap2) > bitSize);
        //image compress
        bitmap2.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        data = baos.toByteArray(); //convert image to bytes

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded
                        progressDialog.dismiss();
                        //get url of uploaded image
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {

                            //add image uri and other info to database
                            DatabaseReference chatRooms = firebaseDatabase.getReference("ChatRooms").child(chatRoomId);
                            String timestamp = String.valueOf(System.currentTimeMillis());

                            //setup required data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", myUid);
                            hashMap.put("receiver", hisUid);
                            hashMap.put("message", downloadUri);
                            hashMap.put("timestamp", timeStamp);
                            hashMap.put("type", "image");
                            hashMap.put("isSeen", false);
                            //put this data to firebase
                            chatRooms.child(timestamp).setValue(hashMap);

                            //send notification
                            sendImageMessageRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    ModelUsers user = snapshot.getValue(ModelUsers.class);
                                    String pseudonym = user.getPseudonym();
                                    if (pseudonym == null) {
                                        pseudonym = "user";
                                    }
                                    if (notify) {
                                        senNotification(
                                                hisUid, pseudonym,
                                                getString(com.amit.yoganet.R.string.teenvio));
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                            //create chatlist node/child in firebase database
                            chatListRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        chatListRef1.child("id").setValue(hisUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                            chatListRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        chatListRef2.child("id").setValue(myUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void senNotification(String hisUid, String name, String message) {
        DatabaseReference allTokens = firebaseDatabase.getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(
                            ""+myUid,
                            ""+name+": "+message,
                            getString(com.amit.yoganet.R.string.newmessage),
                            ""+hisUid,
                            "ChatNotification",
                            hisImage
                    );

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
                        //add this request to queue
                        requestQueue.add(jsonObjectRequest);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.amit.yoganet.R.menu.menu_main, menu);
        //hide searchview, add post, as we dont need it here
        menu.findItem(com.amit.yoganet.R.id.action_search).setVisible(false);
        menu.findItem(com.amit.yoganet.R.id.action_add_post).setVisible(false);
        menu.findItem(com.amit.yoganet.R.id.action_create_group).setVisible(false);

        //hide addpost icon from this fragment
        menu.findItem(com.amit.yoganet.R.id.action_add_participant).setVisible(false);
        menu.findItem(com.amit.yoganet.R.id.action_groupinfo).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //get item id's
        int id = item.getItemId();
        if (id == com.amit.yoganet.R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if (id == R.id.action_settings) {
            //go to settings activity
            startActivity(new Intent(ChattingActivity.this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }



    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recordAudio();
            }
        } else if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    readMessages();
                }
            }
        }
    }
