package com.android.yoganetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class AddPostActivity extends AppCompatActivity {
    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    //storage
    StorageReference storageReference;

     ActionBar actionBar;



    //views
    EditText titleEt, descriptionEt;
    ImageView imageIv;
    VideoView videoView;
    Button uploadBtn;
    //likes
    String pLikes = "0";
    String pComments = "0";
    //user info
    String pseudonym, practic, uid, dp;
    boolean isVideo = false;
    boolean isAudio = false;

    //info of post to be edited
    String editTitle, editDescription, editImage;

    //image picked will be samed in this uri
    Uri image_rui = null;

    private static final int VIDEO_PICK_GALLERY_CODE = 100;
    private static final int VIDEO_PICK_CAMERA_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;

    private String[] cameraPermissions;

    private Uri videoUri = null;

    //progress bar
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.addpost));
        //enable back button in actionbar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
        Query querye = databaseReference.orderByChild("uid").equalTo(user.getUid());
        querye.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    String name = ""+ ds.child("pseudonym").getValue();
                    String practica = ""+ ds.child("practic").getValue();
                    dp = ""+ ds.child("image").getValue();
                    //set data
                    pseudonym = name;
                    practic = practica;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        pd = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();
        //init views
        titleEt = findViewById(R.id.pTitleEt);
        descriptionEt = findViewById(R.id.pDescriptionEt);
        imageIv = findViewById(R.id.pImageIv);
        uploadBtn = findViewById(R.id.pUploadBtn);
        videoView = findViewById(R.id.videoView);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};




        //get data through intent from previous activities
        Intent intent = getIntent();

        //get data and its type from intent
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type!=null){
            if ("text/plain".equals(type)) {
                //text type data
                handleSendText(intent);
            }
            else if(type.startsWith("image")) {
                //image type data
                handleSendImage(intent);
            }
        }




        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_PERMISSION_STORAGE = 100;
            String[] permissions = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    this.requestPermissions(permissions, REQUEST_CODE_PERMISSION_STORAGE);
                    return;
                }
            }
        }

            String isUpdateKey = ""+intent.getStringExtra("key");
        String editPostId = ""+intent.getStringExtra("editPostId");
        //validate if we came here to update post i.e. came from AdapterPost
        if (isUpdateKey.equals("editPost")) {
            //update
            actionBar.setTitle(getString(R.string.updatepost));
            uploadBtn.setText(getString(R.string.actualizar));
            loadPostData(editPostId);
        }
        else {
            //add
            actionBar.setTitle("Añadir nuevo post");
            uploadBtn.setText(getString(R.string.publicar));
        }

        actionBar.setSubtitle(practic);


        //get image from camera/gallery on click
        imageIv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showMediaPickDialog();




            }
        });

        //upload button click listener
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //get data(title, description) from EditTexts
                String title = titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(AddPostActivity.this, getString(R.string.ponertit), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(description)) {
                    Toast.makeText(AddPostActivity.this, getString(R.string.ponerdescr), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isUpdateKey.equals("editPost")) {
                    beginUpdate(title, description, editPostId);
                }
                else {
                    uploadData(title, description);
                }



            }
        });
    }

    private void showMediaPickDialog() {
        //options to show in dialog
        String[] options = {"Añadir imagen", "Añadir vídeo",
                "Añadir audio"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(AddPostActivity.this);
        //set title
        builder.setTitle("Añadir contenido");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //handle dialog items clicks
                if(which == 0){
                    pd.setMessage("Añadiendo imagen...");
                    showImagePickDialog();
                }
                else if (which == 1){
                    pd.setMessage("Añadiendo vídeo...");
                    isVideo = true;
                    videoPickDialog();


                }
                else if (which == 2){
                    pd.setMessage("Añadiendo audio...");
                }
            }
        });
        //create and show dialog
        builder.create().show();


    }

    private void videoPickDialog() {
        String[] options = {"Camera", "Gallery"};

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick video from")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       if (i==0) {
                           //camera
                           if (!checkCameraPermission()) {
                               requestCameraPermission();
                           } else {
                               videoPickCamera();
                           }

                       }
                       else if (i == 1) {
                           //gallery
                           videoPickGallery();
                       }
                    }
                })
                .show();

    }



    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED;

        return result1 && result2;
    }

    private void videoPickGallery() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select video"), VIDEO_PICK_GALLERY_CODE);
    }

    private void videoPickCamera(){

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, VIDEO_PICK_CAMERA_CODE);
    }

    private void handleSendImage(Intent intent) {
        //handle the received image (uri)
        Uri imageURI = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageURI != null) {
            //set to imageview
            imageIv.setImageURI(image_rui);
        }
    }

    private void handleSendText(Intent intent) {
        //handle the received text
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText!= null) {
            //set to description edit text
            descriptionEt.setText(sharedText);
        }
    }

    private void beginUpdate(String title, String description, String editPostId) {
        pd.setMessage(getString(R.string.actualizando));
        pd.show();

        if(!editImage.equals("noImage")) {
            //with content
        updateWasWithImage(title, description, editPostId);
        }
        else if (imageIv.getDrawable() != null || videoUri != null) {
            //with content
        updateWithNewImage(title, description, editPostId);
        }
        else {
            //without content
            updateWithoutImage(title, description, editPostId);
        }
    }

    private void updateWithoutImage(String title, String description, String editPostId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        //put post info
        hashMap.put("uid", uid);
        hashMap.put("uPseudonym", pseudonym);
        hashMap.put("uPractic", practic);
        hashMap.put("uDp", dp);
        hashMap.put("pTitle", title);
        hashMap.put("pDescr", description);
        hashMap.put("pImage", "noImage");
        //path to store post data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //put data in this ref
        ref.child(editPostId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added in database
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, getString(R.string.actpost), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddPostActivity.this, DashboardActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateWithNewImage(String title, String description, String editPostId) {
        if (isVideo) {

        }
        else {


            String timeStamp = String.valueOf(System.currentTimeMillis());
            String filePathAndName = "Posts/" + "post_" + timeStamp;

            //get image from imageview
            Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //image compress
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            byte[] data = baos.toByteArray();

            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded get its url
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;

                            String downloadUri = uriTask.getResult().toString();
                            if (uriTask.isSuccessful()) {
                                //uri is received, upload to firebase database

                                HashMap<String, Object> hashMap = new HashMap<>();
                                //put post info
                                hashMap.put("uid", uid);
                                hashMap.put("uPseudonym", pseudonym);
                                hashMap.put("uPractic", practic);
                                hashMap.put("uDp", dp);
                                hashMap.put("pTitle", title);
                                hashMap.put("pDescr", description);
                                hashMap.put("pImage", downloadUri);
                                //path to store post data
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                //put data in this ref
                                ref.child(editPostId)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //added in database
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, "" + R.string.postactualizado, Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(AddPostActivity.this, DashboardActivity.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }

    private void updateWasWithImage(String title, String description, String editPostId) {
        //post is with image, delete previous image first
        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, upload new image
                        //for post-image name, post-id, publish-time
                        String timeStamp = String.valueOf(System.currentTimeMillis());
                        String filePathAndName = "Posts/"+ "post_"+timeStamp;

                        //get image from imageview
                        Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        //image compress
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
                        byte[] data = baos.toByteArray();

                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                        ref.putBytes(data)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                   //image uploaded get its url
                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful());

                                        String downloadUri = uriTask.getResult().toString();
                                        if (uriTask.isSuccessful()){
                                            //uri is received, upload to firebase database

                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            //put post info
                                            hashMap.put("uid", uid);
                                            hashMap.put("uPseudonym", pseudonym);
                                            hashMap.put("uPractic", practic);
                                            hashMap.put("uDp", dp);
                                            hashMap.put("pTitle", title);
                                            hashMap.put("pDescr", description);
                                            hashMap.put("pImage", downloadUri);
                                            //path to store post data
                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                            //put data in this ref
                                            ref.child(editPostId)
                                                    .updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            //added in database
                                                            pd.dismiss();
                                                            Toast.makeText(AddPostActivity.this, getString(R.string.postactualizado), Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(AddPostActivity.this, DashboardActivity.class));
                                                            finish();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            pd.dismiss();
                                                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                           pd.dismiss();
                                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        //get detail of post using id of post
        Query fquery = reference.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    editTitle = ""+ds.child("pTitle").getValue();
                    editDescription = ""+ds.child("pDescr").getValue();
                    editImage = ""+ds.child("pImage").getValue();

                    //set data to views
                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDescription);

                    //set image
                    if (!editImage.equals("noImage")) {
                        try {
                            Picasso.get().load(editImage).into(imageIv);
                        }
                        catch (Exception e) {

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void uploadData(String title, String description) {

        //for post-image name, post-id, post-publish-time
        final String timeStamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "Posts/" + "post_" + timeStamp;
        if (imageIv.getDrawable() != null) {
            //get image from imageview
            Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //image compress
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            byte[] data = baos.toByteArray();


            //post with image
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image is uploaded to firebase storage, now get it's url
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());

                            String downloadUri = uriTask.getResult().toString();
                            if (uriTask.isSuccessful()) {
                                //url is received upload post to firebase database

                                HashMap<Object, String> hashMap = new HashMap<>();
                                //put post info
                                hashMap.put("uid", uid);
                                hashMap.put("uPseudonym", pseudonym);
                                hashMap.put("uPractic", practic);
                                hashMap.put("uDp", dp);
                                hashMap.put("pId", timeStamp);
                                hashMap.put("pTitle", title);
                                hashMap.put("pLikes", pLikes);
                                hashMap.put("pComments", pComments);
                                hashMap.put("pDescr", description);
                                hashMap.put("pImage", downloadUri);
                                hashMap.put("pTime", timeStamp);
                                //path to store post data
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                //put data in this ref
                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //added in database
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, ""+R.string.publicado, Toast.LENGTH_SHORT).show();
                                                //reset views
                                                titleEt.setText("");
                                                descriptionEt.setText("");
                                                imageIv.setImageURI(null);
                                                image_rui = null;

                                                //send notification
                                                prepareNotification(
                                                        ""+timeStamp,
                                                        ""+pseudonym+" añadió un post nuevo!",
                                                        ""+title+"\n"+description,
                                                        "PostNotification",
                                                        "POST"
                                                );
                                                startActivity(new Intent(AddPostActivity.this, DashboardActivity.class));
                                                finish();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed adding post in database

                                                Log.e("YogaNetwork", "failed uploading post to database", e);
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed uploading image
                            Log.e("YogaNetwork", "failed uploading image", e);
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else if (videoUri != null) {
            /*pd.setMessage("Loading...");
            pd.setCanceledOnTouchOutside(false);*/

            uploadVideoFirebase(title, description);
        }

        else {
            //post without image
            HashMap<Object, String> hashMap = new HashMap<>();
            //put post info
            hashMap.put("uid", uid);
            hashMap.put("uPseudonym", pseudonym);
            hashMap.put("uPractic", practic);
            hashMap.put("pLikes", pLikes);
            hashMap.put("pComments", pComments);
            hashMap.put("uDp", dp);
            hashMap.put("pId", timeStamp);
            hashMap.put("pTitle", title);
            hashMap.put("pDescr", description);
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", timeStamp);
            //path to store post data
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            //put data in this ref
            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //added in database
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, R.string.publicado, Toast.LENGTH_SHORT).show();
                            titleEt.setText("");
                            descriptionEt.setText("");
                            imageIv.setImageURI(null);
                            image_rui = null;

                            //send notification
                            prepareNotification(
                                    ""+timeStamp,
                                    ""+pseudonym+getString(R.string.añadio),
                                    ""+title+"\n"+description,
                                    "PostNotification",
                                    "POST"
                            );
                            startActivity(new Intent(AddPostActivity.this, DashboardActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed adding post in database

                            Log.e("YogaNetwork", "failed uploading post to database", e);
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void uploadVideoFirebase(String title, String description) {
        /*pd.show();*/

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            new CompressVideo().execute("false",videoUri.toString(),file.getPath());


/*
        String timestamp = ""+System.currentTimeMillis();
        String filePathAndName = "Posts/" + "post_" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

        storageReference.putFile(videoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(AddPostActivity.this, "Nice", Toast.LENGTH_SHORT).show();*/

              /*  })
                .addOnFailureListener(e -> {

                });*/

    }


    //call this method whenever you publish post
    private void prepareNotification(String pId, String title, String description, String notificationType, String notificationTopic) {
        //prepapre data for notification

      String NOTIFICATION_TOPIC = "/topics/" + notificationTopic; //topic must match with what the receiver subscribed to
        String NOTIFICATION_TITLE = title; //i.e. Alex added new post
        String NOTIFICATION_MESSAGE = description; //content of post
        String NOTIFICATION_TYPE = notificationType; //now there are two notification types chat & post, so to differentiate in FirebaseMessaging.java class

        //prepare json what to send, and where to send
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            //what to send
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("sender", uid); //uid of current user/sender
            notificationBodyJo.put("pId", pId); //post id
            notificationBodyJo.put("pTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("pDescription", NOTIFICATION_MESSAGE);
            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJo); //combine data to be sent
        } catch (JSONException e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        sendPostNotification(notificationJo);
    }

    private void sendPostNotification(JSONObject notificationJo) {
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
                        Toast.makeText(AddPostActivity.this, "uuu"+error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=AAAAQamk8xY:APA91bGUIhTv9TUj9ST-1Z-1uCkgfszxVP3I_yrOeo7Xuudb-zbW73Usx6yD5afnYE-fZqPIJIsvBXgCo11JUAXv1vLU8cZSOS7UTRcYVrhTlvw3oGzGlwuapE0m1gV6qWgjYMM20Oms");
                return headers;
            }
        };
        //enqueue the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void showImagePickDialog() {
        CropImage.activity()
                .start(this);
    }

    private void setVideoToVideoView() {
        imageIv.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);


        videoView.setVideoURI(videoUri);
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
                        MediaController mc = new MediaController(AddPostActivity.this);
                        videoView.setMediaController(mc);
                        mc.setAnchorView(videoView);
                    }
                });
                videoView.start();
            }
        });
    }

    private void setCompressedVideoToVideoView(Uri uri) {
        imageIv.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);

        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
                        MediaController mc = new MediaController(AddPostActivity.this);
                        videoView.setMediaController(mc);
                        mc.setAnchorView(videoView);
                    }
                });


                videoView.start();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length>0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        videoPickCamera();
                    }
                    else {
                        Toast.makeText(AddPostActivity.this, "No permissions!", Toast.LENGTH_SHORT).show();
                    }

                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here

         uid = user.getUid();



        }
        else {
            //user not signed in, go to mainactivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //goto previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id's
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();

        }
        return super.onOptionsItemSelected(item);
    }

    //handle permission results


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {



                Uri resultUri = result.getUri();
                imageIv.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                System.out.println(error);
            }
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == VIDEO_PICK_GALLERY_CODE){
                videoUri = data.getData();
                //show picked video in videoview
                setVideoToVideoView();
            }
            else if (requestCode == VIDEO_PICK_CAMERA_CODE) {
                videoUri = data.getData();
                setVideoToVideoView();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private class CompressVideo extends AsyncTask<String,String,String> {
        Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = ProgressDialog.show(AddPostActivity.this, "", "Compressing...");
        }

        @Override
        protected String doInBackground(String... strings) {
            String videoPath = null;
            try {
             Uri uri = Uri.parse(strings[1]);

             videoPath = SiliCompressor.with(AddPostActivity.this)
                     .compressVideo(uri,strings[2]);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }


            return videoPath;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            dialog.dismiss();

            File file = new File(s);

            Uri uri = Uri.fromFile(file);
            setCompressedVideoToVideoView(uri);

            String timestamp = ""+System.currentTimeMillis();
            String filePathAndName = "Posts/" + "post_" + timestamp;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

            storageReference.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(AddPostActivity.this, "Done!", Toast.LENGTH_SHORT).show();

                        //video is uploaded to firebase storage, now get it's url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String title = titleEt.getText().toString();
                        String description = descriptionEt.getText().toString();


                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            //url is received upload post to firebase database

                            HashMap<Object, String> hashMap = new HashMap<>();
                            //put post info
                            hashMap.put("uid", uid);
                            hashMap.put("uPseudonym", pseudonym);
                            hashMap.put("uPractic", practic);
                            hashMap.put("uDp", dp);
                            hashMap.put("pId", timestamp);
                            hashMap.put("pTitle", title);
                            hashMap.put("pLikes", pLikes);
                            hashMap.put("pComments", pComments);
                            hashMap.put("pDescr", description);
                            hashMap.put("pVideo", downloadUri);
                            hashMap.put("pTime", timestamp);
                            //path to store post data
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            //put data in this ref
                            ref.child(timestamp).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //added in database
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, ""+R.string.publicado, Toast.LENGTH_SHORT).show();
                                            //reset views
                                            titleEt.setText("");
                                            descriptionEt.setText("");
                                            videoView.setVideoURI(null);
                                            videoUri = null;

                                            //send notification
                                            prepareNotification(
                                                    ""+timestamp,
                                                    ""+pseudonym+" añadió un post nuevo!",
                                                    ""+title+"\n"+description,
                                                    "PostNotification",
                                                    "POST"
                                            );
                                            startActivity(new Intent(AddPostActivity.this, DashboardActivity.class));
                                            finish();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //failed adding post in database

                                            Log.e("YogaNetwork", "failed uploading post to database", e);
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }

                    })
                    .addOnFailureListener(e -> {
                        pd.dismiss();
                        //failed adding post in storage
                        Log.e("YogaNetwork", "failed uploading post to database", e);
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    });


        }
    }
}

