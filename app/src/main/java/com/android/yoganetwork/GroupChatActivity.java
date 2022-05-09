package com.android.yoganetwork;

import android.graphics.Bitmap;
import android.graphics.Outline;
import android.media.MediaRecorder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.BitmapCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
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

import com.android.yoganetwork.ChatView.AttachmentOption;
import com.android.yoganetwork.ChatView.AttachmentOptionsListener;
import com.android.yoganetwork.ChatView.AudioRecordView;
import com.android.yoganetwork.adapters.AdapterGroupChat;
import com.android.yoganetwork.models.ModelGroupChat;
import com.android.yoganetwork.models.ModelUsers;
import com.android.yoganetwork.notifications.Data;
import com.android.yoganetwork.utils.ImageUtils;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
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
import com.android.yoganetwork.crop.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class  GroupChatActivity extends AppCompatActivity implements AudioRecordView.RecordingListener, View.OnClickListener, AttachmentOptionsListener {

  private static final int PICK_AUD_REQUEST = 103;
  private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 104;
  private FirebaseAuth firebaseAuth;

  private String groupId, myGroupRole = "";

  private Toolbar toolbar;
  private ImageView groupIconIv;


  private ImageButton attachBtn, sendBtn;
  private TextView groupTitleTv;
  private EditText messageEt;
  private RecyclerView recyclerViewMessages;
  private RelativeLayout toolbar_layout;
  private ArrayList<ModelGroupChat> groupChatList;
  private AdapterGroupChat adapterGroupChat;
  private AudioRecordView audioRecordView;
  private String audioFileName, audioFilePath;
  private MediaRecorder recorder;


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
  private Uri image_uri = null, audioPath = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_group_chat);
    audioRecordView = new AudioRecordView();
    audioRecordView.initView(findViewById(R.id.layoutMain));
    View containerView = audioRecordView.setContainerView(R.layout.layout_groupchat);
    audioRecordView.setRecordingListener(this);
    recyclerViewMessages = containerView.findViewById(R.id.chatRv);

    //init views
    toolbar = findViewById(R.id.toolbar);
    toolbar_layout = findViewById(R.id.toolbar_layout);
    groupIconIv = findViewById(R.id.groupIconIv);
    //attachBtn = findViewById(R.id.attachBtn);
    groupTitleTv = findViewById(R.id.groupTitleTv);
    //sendBtn = findViewById(R.id.sendBtn);
    //messageEt = findViewById(R.id.messageEt);

    setSupportActionBar(toolbar);

    toolbar_layout.setOnClickListener(v -> {

      Intent intent = new Intent(this, GroupInfoActivity.class);
      intent.putExtra("groupId", groupId);
      startActivity(intent);
    });

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

    int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
    } else {
      loadGroupMessages();
    }


    loadMyGroupRole();


    audioRecordView.getSendView().setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String message = audioRecordView.getMessageView().getText().toString().trim();
        audioRecordView.hideAttachmentOptionView();
        //get text from edit text
        //check if text is empty or not
        if (TextUtils.isEmpty(message)) {
          //text empty
          Toast.makeText(GroupChatActivity.this, R.string.emptymsg, Toast.LENGTH_SHORT).show();
        } else {
          //text not empty
          sendMesage(message);
        }
        //reset edittext after sending message
        audioRecordView.getMessageView().setText("");

      }


    });

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(GroupChatActivity.this, LinearLayoutManager.VERTICAL, false);
    linearLayoutManager.setStackFromEnd(true);
    recyclerViewMessages.setLayoutManager(linearLayoutManager);
    recyclerViewMessages.setHasFixedSize(false);

    //adapter
    adapterGroupChat = new AdapterGroupChat(GroupChatActivity.this, groupChatList, groupId);
    //set to recyclerview
    recyclerViewMessages.setAdapter(adapterGroupChat);
    recyclerViewMessages.smoothScrollToPosition(recyclerViewMessages.getAdapter().getItemCount());



    audioRecordView.getMessageView().requestFocus();

    audioRecordView.setAttachmentOptions(AttachmentOption.getList(), this);

    audioRecordView.removeAttachmentOptionAnimation(false);


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


  }

  private void sendImageMessage() throws IOException {
    //progress dialog
    ProgressDialog pd = new ProgressDialog(this);
    pd.setTitle(getString(R.string.wait));
    pd.setMessage(getString(R.string.enviandoimg));
    pd.setCanceledOnTouchOutside(false);
    pd.show();

    //file name and path in firebase storage
    String filenamePath = "ChatImages/" + "" + System.currentTimeMillis();

    StorageReference storageReference = FirebaseStorage.getInstance().getReference(filenamePath);


    int bitSize = 4000000;
    int quality = 70;


    //get image from imageview
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] data = baos.toByteArray();
    Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
    Bitmap bitmap2;
    int n = 1;
    do {
      bitmap2 = new ImageUtils().getResizedBitmap(bitmap1, bitmap1.getWidth() / n, bitmap1.getHeight() / n);
      if (n % 2 != 0) {
        n++;
      } else {
        n += 2;
      }
    } while (BitmapCompat.getAllocationByteCount(bitmap2) > bitSize);
    //image compress
    bitmap2.compress(Bitmap.CompressFormat.JPEG, quality, baos);
    data = baos.toByteArray(); //convert image to bytes


    //upload image
    storageReference.putBytes(data)
            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
              @Override
              public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image uploaded, get url

                Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!p_uriTask.isSuccessful()) ;
                Uri p_downloadUri = p_uriTask.getResult();

                if (p_uriTask.isSuccessful()) {
                  //image url received, save in db
                  //timestamp
                  String timestamp = "" + System.currentTimeMillis();

                  //setup message data
                  HashMap<String, Object> hashMap = new HashMap<>();
                  hashMap.put("sender", "" + firebaseAuth.getUid());
                  hashMap.put("message", "" + p_downloadUri);
                  hashMap.put("timestamp", "" + timestamp);
                  hashMap.put("type", "" + "image"); //text/image/file

                  //add in db
                  DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                  ref.child(groupId).child("Messages").child(timestamp)
                          .setValue(hashMap)
                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                              //message sent
                              //clear messageEt
                              pd.dismiss();
                            }
                          })
                          .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                              //message sending failed
                              pd.dismiss();
                              Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                          });
                }
              }
            })
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                //failed uploading image
                Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                for (DataSnapshot ds : snapshot.getChildren()) {
                  myGroupRole = "" + ds.child("role").getValue();
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
                for (DataSnapshot ds : snapshot.getChildren()) {
                  ModelGroupChat model = ds.getValue(ModelGroupChat.class);
                  groupChatList.add(model);
                }
                //adapter
                adapterGroupChat.setChatList(groupChatList);
                adapterGroupChat.notifyItemInserted(groupChatList.size() - 1);

                //set to recyclerview
                recyclerViewMessages.setAdapter(adapterGroupChat);
                recyclerViewMessages.smoothScrollToPosition(recyclerViewMessages.getAdapter().getItemCount());

              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
            });
  }

  private void sendMesage(String message) {
    //timestamp
    String timestamp = "" + System.currentTimeMillis();

    //setup message data
    HashMap<String, Object> hashMap = new HashMap<>();
    hashMap.put("sender", "" + firebaseAuth.getUid());
    hashMap.put("message", "" + message);
    hashMap.put("timestamp", "" + timestamp);
    hashMap.put("type", "" + "text"); //text/image/file

    //add in db
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
    ref.child(groupId).child("Messages").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void aVoid) {
                //message sent
                //clear messageEt
              }
            })
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                //message sending failed
                Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
              }
            });
  }

  private void loadGroupInfo() {
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
    ref.orderByChild("groupId").equalTo(groupId)
            .addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                  String groupTitle = "" + ds.child("groupTitle").getValue();
                  String groupDescription = "" + ds.child("groupDescription").getValue();
                  String groupIcon = "" + ds.child("groupIcon").getValue();
                  String timestamp = "" + ds.child("timestamp").getValue();
                  String createdBy = "" + ds.child("createdBy").getValue();

                  groupTitleTv.setText(groupTitle);
                  Glide.with(getApplication().getApplicationContext()).load(groupIcon).placeholder(R.drawable.ic_group_white).into(groupIconIv);


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
    } else {
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
    } else if (id == R.id.action_groupinfo) {
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
      if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
        CropImage.ActivityResult result = CropImage.getActivityResult(data);


        assert result != null;
        image_uri = result.getUri();
        try {
          sendImageMessage();
        } catch (IOException e) {
          e.printStackTrace();
        }

      } else if (requestCode == PICK_AUD_REQUEST && data != null && data.getData() != null) {
        //Toast.makeText(this, "Got data", Toast.LENGTH_SHORT).show();
        audioPath = data.getData();

        uploadAudio();
      }
    }
  }


  @Override
  public void onClick(View view) {


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

  private void showImagePickDialog() {
    CropImage.activity()
            .start(this);

  }

  @Override
  public void onRecordingStarted() {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
              10);
    } else {
      //start recording audio
      recordAudio();
    }

  }

  private void recordAudio() {
    //create file to record audio
    audioFileName = UUID.randomUUID().toString() + ".3gp";
    audioFilePath = GroupChatActivity.this.getExternalFilesDir(null).getAbsolutePath() + "/" + audioFileName;
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
      System.out.println("Error" + e.getMessage());
    }


  }

  @Override
  public void onRecordingLocked() {
  }


  @Override
  public void onRecordingCompleted() {
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
      } catch (RuntimeException stopException) {
        // handle cleanup here
      }

    }

  }

  private void uploadAudio() {
    //create storage reference
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    String timestamp = String.valueOf(System.currentTimeMillis());
    Uri audioFile = null;
    if (audioFilePath != null) {
      //create file reference
      audioFile = Uri.fromFile(new File(audioFilePath));
    } else if (audioPath != null) {
      audioFile = audioPath;
    }
    //create file reference
    final StorageReference fileReference = storageReference.child("GroupAudios/" + timestamp + ".3gp");
    //upload file
    assert audioFile != null;
    fileReference.putFile(audioFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
      @Override
      public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        //get url of audio
        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
          @Override
          public void onSuccess(Uri uri) {

            //timestamp
            String timestamp = "" + System.currentTimeMillis();
            String url = uri.toString();

            //setup message data
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", "" + firebaseAuth.getUid());
            hashMap.put("message", "" + url);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("type", "audio"); //text/image/file

            //add in db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
            ref.child(groupId).child("Messages").child(timestamp)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                      @Override
                      public void onSuccess(Void aVoid) {
                        //message sent
                        //clear messageEt
                      }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                        //message sending failed
                        Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        Toast.makeText(GroupChatActivity.this, "Failed to upload audio", Toast.LENGTH_SHORT).show();
      }
    });
  }



    @Override
  public void onRecordingCanceled() {
  }

  @Override
  public void onPointerCaptureChanged(boolean hasCapture) {
    super.onPointerCaptureChanged(hasCapture);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
      if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
        loadGroupMessages();
      }
    }
  }
}