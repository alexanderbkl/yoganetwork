package com.android.yoganetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.yoganetwork.adapters.AdapterGroupChat;
import com.android.yoganetwork.models.ModelGroupChat;
import com.android.yoganetwork.notifications.Data;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

  private FirebaseAuth firebaseAuth;

  private String groupId, myGroupRole="";

  private Toolbar toolbar;
  private ImageView groupIconIv;


  private ImageButton attachBtn, sendBtn;
  private TextView groupTitleTv;
  private EditText messageEt;
  private RecyclerView chatRv;

  private ArrayList<ModelGroupChat> groupChatList;
  private AdapterGroupChat adapterGroupChat;

  //permissions request constants
  private static final int CAMERA_REQUEST_CODE = 200;
  private static final int STORAGE_REQUEST_CODE = 400;
  //image pick constants
  private static final int IMAGE_PICK_GALLERY_CODE = 1000;
  private static final int IMAGE_PICK_CAMERA_CODE = 2000;
  //permissions to be requested
  private String[] cameraPermission;
  private String[] storagePermission;
  //uri of picked image
  private Uri image_uri = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_group_chat);

    //init views
    toolbar = findViewById(R.id.toolbar);
    groupIconIv = findViewById(R.id.groupIconIv);
    attachBtn = findViewById(R.id.attachBtn);
    groupTitleTv = findViewById(R.id.groupTitleTv);
    sendBtn = findViewById(R.id.sendBtn);
    messageEt = findViewById(R.id.messageEt);
    chatRv = findViewById(R.id.chatRv);

    setSupportActionBar(toolbar);



    //get id of the group
    Intent intent = getIntent();
    groupId = intent.getStringExtra("groupId");

    //init requirements
    cameraPermission = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    storagePermission = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    firebaseAuth = FirebaseAuth.getInstance();
    loadGroupInfo();
    loadGroupMessages();
    loadMyGroupRole();



    sendBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //input data
        String message = messageEt.getText().toString().trim();
        //validate
        if (TextUtils.isEmpty(message)){
          //empty, don't send
          Toast.makeText(GroupChatActivity.this, R.string.emptymsg, Toast.LENGTH_SHORT).show();
        }
        else {
          //send message
          sendMesage(message);
        }
      }
    });

    attachBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //attach image
        showImageImportDialog();
      }
    });

  }

  private void showImageImportDialog() {
    //options to display
    String[] options = {getString(R.string.camera), getString(R.string.galeria)};

    //dialog
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.escogerimg1)
            .setItems(options, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                //handle clicks
                if (which == 0) {
                  //camera clicked
                  if (!checkCameraPermission()) {
                    //not granted, request
                    requestCameraPermission();
                  }
                  else {
                    //already granted
                    pickCamera();
                  }
                }
                else {
                  //gallery clicked
                  if(!checkStoragePermission()) {
                    //not granted, request
                    requestStoragePermission();
                  }
                  else {
                    //already granted
                    pickGallery();
                  }
                }
              }
            }).show();
  }

  private void pickGallery(){
    //intent to pick image from gallery
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType("image/*");
    startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
  }

  private void pickCamera(){
    ContentValues contentValues = new ContentValues();
    contentValues.put(MediaStore.Images.Media.TITLE, "GroupImageTitle");
    contentValues.put(MediaStore.Images.Media.DESCRIPTION, "GroupImageDescription");

    image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
    startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE );
  }

  private void requestStoragePermission() {
    ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
  }

  private boolean checkStoragePermission() {
    boolean result = ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    return result;
  }
  private void requestCameraPermission() {
    ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
  }

  private boolean checkCameraPermission() {
    boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
    boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    return result && result1;
  }

  private void sendImageMessage() {
    //progress dialog
    ProgressDialog pd = new ProgressDialog(this);
    pd.setTitle(getString(R.string.wait));
    pd.setMessage(getString(R.string.enviandoimg));
    pd.setCanceledOnTouchOutside(false);
    pd.show();

    //file name and path in firebase storage
    String filenamePath = "ChatImages/"+""+System.currentTimeMillis();

    StorageReference storageReference = FirebaseStorage.getInstance().getReference(filenamePath);
    //upload image
    storageReference.putFile(image_uri)
            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
              @Override
              public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image uploaded, get url

                Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!p_uriTask.isSuccessful());
                Uri p_downloadUri = p_uriTask.getResult();

                if (p_uriTask.isSuccessful()) {
                  //image url received, save in db
                  //timestamp
                  String timestamp = ""+System.currentTimeMillis();

                  //setup message data
                  HashMap<String, Object> hashMap = new HashMap<>();
                  hashMap.put("sender", ""+firebaseAuth.getUid());
                  hashMap.put("message", ""+ p_downloadUri);
                  hashMap.put("timestamp", ""+timestamp);
                  hashMap.put("type", ""+"image"); //text/image/file

                  //add in db
                  DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                  ref.child(groupId).child("Messages").child(timestamp)
                          .setValue(hashMap)
                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                              //message sent
                              //clear messageEt
                              messageEt.setText("");
                              pd.dismiss();
                            }
                          })
                          .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                              //message sending failed
                              pd.dismiss();
                              Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                          });
                }
              }
            })
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                //failed uploading image
                Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
              }
            });
  }

  private void loadMyGroupRole() {
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
    ref.child(groupId).child("Participants")
            .orderByChild("uid").equalTo(firebaseAuth.getUid())
            .addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                  myGroupRole = ""+ds.child("role").getValue();
                  //refresh menu items
                  invalidateOptionsMenu();
                }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
            });
  }

  private void loadGroupMessages() {
    //init list
    groupChatList = new ArrayList<>();

    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
    ref.child(groupId).child("Messages")
            .addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                  ModelGroupChat model = ds.getValue(ModelGroupChat.class);
                  groupChatList.add(model);
                }
                //adapter
                adapterGroupChat = new AdapterGroupChat(GroupChatActivity.this, groupChatList);
                //set to recyclerview
                chatRv.setAdapter(adapterGroupChat);
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
            });
  }

  private void sendMesage(String message) {
    //timestamp
    String timestamp = ""+System.currentTimeMillis();

    //setup message data
    HashMap<String, Object> hashMap = new HashMap<>();
    hashMap.put("sender", ""+firebaseAuth.getUid());
    hashMap.put("message", ""+message);
    hashMap.put("timestamp", ""+timestamp);
    hashMap.put("type", ""+"text"); //text/image/file

    //add in db
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
    ref.child(groupId).child("Messages").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void aVoid) {
                //message sent
                //clear messageEt
                messageEt.setText("");
              }
            })
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                //message sending failed
                Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
              }
            });
  }

  private void loadGroupInfo() {
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
    ref.orderByChild("groupId").equalTo(groupId)
            .addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                  String groupTitle= ""+ds.child("groupTitle").getValue();
                  String groupDescription= ""+ds.child("groupDescription").getValue();
                  String groupIcon= ""+ds.child("groupIcon").getValue();
                  String timestamp= ""+ds.child("timestamp").getValue();
                  String createdBy= ""+ds.child("createdBy").getValue();

                  groupTitleTv.setText(groupTitle);
                  try {
                    Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_white).into(groupIconIv);
                  } catch (Exception e) {
                    groupIconIv.setImageResource(R.drawable.ic_group_white);
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
    getMenuInflater().inflate(R.menu.menu_main, menu);

    menu.findItem(R.id.action_settings).setVisible(false);
    menu.findItem(R.id.action_create_group).setVisible(false);
    menu.findItem(R.id.action_add_post).setVisible(false);
    menu.findItem(R.id.action_logout).setVisible(false);

    if (myGroupRole.equals("creator") || myGroupRole.equals("admin")) {
      //im admin/creator, show add person option
      menu.findItem(R.id.action_add_participant).setVisible(true);
    }
    else {
      menu.findItem(R.id.action_add_participant).setVisible(false);
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_add_participant) {
      Intent intent = new Intent(this, GroupParticipantAddActivity.class);
      intent.putExtra("groupId", groupId);
      startActivity(intent);
    }
    else if (id == R.id.action_groupinfo) {
      Intent intent = new Intent(this, GroupInfoActivity.class);
      intent.putExtra("groupId", groupId);
      startActivity(intent);
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK) {
      if (requestCode == IMAGE_PICK_GALLERY_CODE) {
        //picked from gallery
        image_uri = data.getData();
        sendImageMessage();
      }
      if (requestCode == IMAGE_PICK_CAMERA_CODE) {
        //picked from camera
        sendImageMessage();
      }
    }
  }



  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case CAMERA_REQUEST_CODE:
        if (grantResults.length > 0) {
          boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
          boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
          if (cameraAccepted && writeStorageAccepted) {
            pickCamera();
          }
          else {
            Toast.makeText(this, R.string.camerastoragereq, Toast.LENGTH_SHORT).show();
          }
        }
        break;
      case STORAGE_REQUEST_CODE:
        if (grantResults.length>0){
          boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
          if (writeStorageAccepted) {
            pickGallery();
          }
          else {
            Toast.makeText(this, R.string.storagereq, Toast.LENGTH_SHORT).show();
          }
        }
        break;
    }
  }
}