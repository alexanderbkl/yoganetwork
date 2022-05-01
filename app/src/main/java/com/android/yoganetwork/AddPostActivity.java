package com.android.yoganetwork;

import android.graphics.Outline;
import android.util.SparseArray;
import android.view.ViewOutlineProvider;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.core.graphics.BitmapCompat;
import com.ablanco.zoomy.Zoomy;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.android.yoganetwork.crop.CropImage;
import com.android.yoganetwork.utils.ImageUtils;
import com.android.yoganetwork.youtubeExtractor.VideoMeta;
import com.android.yoganetwork.youtubeExtractor.YouTubeExtractor;
import com.android.yoganetwork.youtubeExtractor.YtFile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;


public class AddPostActivity extends AppCompatActivity {
    private static final int PICK_AUD_REQUEST = 103;
    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    //storage
    StorageReference storageReference;

    SimpleExoPlayer simpleExoPlayer;
    PlayerView playerView, player;
    MediaItem mediaItem;
    boolean playWhenReady = true;
    long playbackPosition = 0;
    int currentWindow = 0;
    String youtubeUrl = "";
    String thumbnail = "";

    //views
    EditText titleEt, descriptionEt;
    ImageView imageIv;
    VideoView videoView;
    Button uploadBtn, playBtn;
    //likes
    String pLikes = "0";
    String pDislikes = "0";
    String pComments = "0";
    private Uri audioPath;

    ;
    //user info
    String pseudonym, practic, uid, dp;
    boolean isVideo = false;
    boolean isAudio = false;
    Toolbar toolbar;
    //info of post to be edited
    String editTitle, editDescription, editImage, editVideo;

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

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        toolbar = findViewById(R.id.toolbar_main);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
        setSupportActionBar(toolbar);
        videoView = findViewById(R.id.videoView);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add Post");
        Query querye = databaseReference.orderByChild("uid").equalTo(user.getUid());
        querye.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for (DataSnapshot ds : snapshot.
                        getChildren()) {
                    //get data
                    String name = "" + ds.child("pseudonym").getValue();
                    String practica = "" + ds.child("practic").getValue();
                    dp = "" + ds.child("image").getValue();
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
        playBtn = findViewById(R.id.playBtn);
        playerView = findViewById(R.id.player);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission is granted");
            //File write logic here
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }


        //get data through intent from previous activities
        Intent intent = getIntent();

        //get data and its type from intent
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                //text type data
                handleSendText(intent);
            } else if (type.startsWith("image")) {
                //image type data
                handleSendImage(intent);
            }
        }


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

        String isUpdateKey = "" + intent.getStringExtra("key");
        String editPostId = "" + intent.getStringExtra("editPostId");
        //validate if we came here to update post i.e. came from AdapterPost
        if (isUpdateKey.equals("editPost")) {
            //update
            getSupportActionBar().setTitle(getString(R.string.updatepost));
            uploadBtn.setText(getString(R.string.actualizar));
            loadPostData(editPostId);
        } else {
            //add
            getSupportActionBar().setTitle("Añadir nuevo post");
            uploadBtn.setText(getString(R.string.publicar));
        }

        getSupportActionBar().setSubtitle(practic);


        //get image from camera/gallery on click
        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showMediaPickDialog();

            }
        });
        if(!youtubeUrl.equals("")){
            thumbnail = extractYoutubeThumbnail(youtubeUrl);
            Glide.with(AddPostActivity.this).load(thumbnail).into(imageIv);
        }
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
                } else if (!youtubeUrl.equals("")) {
                    uploadYoutubeEmbed(title, description, youtubeUrl);
                }
                else {
                    uploadData(title, description);
                }


            }
        });
        Zoomy.Builder builder = new Zoomy.Builder(AddPostActivity.this)
                .target(imageIv);
        builder.register();
    }

    private void showMediaPickDialog() {
        //options to show in dialog
        String[] options = {"Añadir imagen", "Añadir vídeo",
                "Añadir audio", "Añadir enlace youtube"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(AddPostActivity.this);
        //set title
        builder.setTitle("Añadir contenido");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //handle dialog items clicks
                if (which == 0) {
                    Snackbar.make(imageIv, "Abriendo imagen", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    showImagePickDialog();
                } else if (which == 1) {
                    Snackbar.make(imageIv, "Abriendo vídeo", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    isVideo = true;
                    videoPickDialog();


                } else if (which == 2) {
                    Snackbar.make(imageIv, "Abriendo explorador", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    chooseAud();
                } else if (which == 3) {
                    Snackbar.make(imageIv, "Abriendo youtube", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    //make dialog to get youtube url from user
                    youtubeUrlDialog();

                }
            }

            private void youtubeUrlDialog() {
                //alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(AddPostActivity.this);
                //set title
                builder.setTitle("Añadir enlace youtube");
                //set layout of dialog
                LinearLayout linearLayout = new LinearLayout(AddPostActivity.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setPadding(10, 10, 10, 10);
                //add edit text
                final EditText youtubeUrlEt = new EditText(AddPostActivity.this);
                youtubeUrlEt.setHint("Introduce el enlace");
                linearLayout.addView(youtubeUrlEt);
                //add button
                Button button = new Button(AddPostActivity.this);
                button.setText("Añadir");
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        youtubeUrl = youtubeUrlEt.getText().toString();
                        if (youtubeUrl.isEmpty()) {
                            Snackbar.make(imageIv, "Introduce un enlace", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        } else {
                            Snackbar.make(imageIv, "Enlace añadido", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            if(!youtubeUrl.equals("")){
                                thumbnail = extractYoutubeThumbnail(youtubeUrl);
                                Glide.with(AddPostActivity.this).load(thumbnail).into(imageIv);
                            }
                        }
                    }
                });
                linearLayout.addView(button);
                //set layout to dialog
                builder.setView(linearLayout);
                //show dialog
                builder.show();
            }
        });
        //create and show dialog
        builder.create().show();


    }

    private void embedYoutube(String youtubeUrl) {
        imageIv.setVisibility(View.VISIBLE);
        if(!youtubeUrl.equals("")){
            thumbnail = extractYoutubeThumbnail(youtubeUrl);
            Glide.with(AddPostActivity.this).load(thumbnail).into(imageIv);
        }

    }

    private String extractYoutubeThumbnail(String youtubeUrl) {
        String videoId = getYoutubeVideoId(youtubeUrl);
        //get thumbnail
        return "https://img.youtube.com/vi/" + videoId + "/0.jpg";
    }

    static String getYoutubeVideoId(String youtubeUrl) {

            String vId = null;
            Pattern pattern = Pattern.compile(
                    "^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(youtubeUrl);
            if (matcher.matches()){
                vId = matcher.group(1);
            }
            if (vId == null) {
                //get text after "=" in url
                vId = youtubeUrl.split("=")[1];
            }
            return vId;

    }

    private void chooseAud() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Aud"), PICK_AUD_REQUEST);
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
            Glide.with(this).load(imageURI).into(imageIv);
        }
    }

    private void handleSendText(Intent intent) {
        //handle the received text
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        //check if sharedtext has youtube url
        if (sharedText.contains("youtube.com") || sharedText.contains("youtu.be")) {
            youtubeUrl = sharedText;
            embedYoutube(youtubeUrl);
        }
        else if (!sharedText.equals("")) {
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

            if (editImage != null && !editImage.equals("noImage") && !editImage.equals("")) {
                StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
                mPictureRef.delete();
            }
            String timeStamp = String.valueOf(System.currentTimeMillis());
            String filePathAndName = "Posts/" + "post_" + timeStamp;

            int bitSize = 1000000;
            int quality = 80;



            //get image from imageview
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] data = baos.toByteArray();
            Bitmap bitmap1 = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
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
            data = baos.toByteArray();




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
        if (editImage != null && !editImage.equals("") && !editImage.equals("noImage")) {


        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, upload new image
                        //for post-image name, post-id, publish-time
                        String timeStamp = String.valueOf(System.currentTimeMillis());
                        String filePathAndName = "Posts/"+ "post_"+timeStamp;

                        int bitSize = 1000000;
                        int quality = 90;



                        //get image from imageview
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] data = baos.toByteArray();
                        Bitmap bitmap1 = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
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
                        data = baos.toByteArray();


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
                });}
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
                    editImage = ds.child("pImage").getValue(String.class);
                    editVideo = ds.child("pVideo").getValue(String.class);


                    //set data to views
                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDescription);

                    //set image
                    if (!Objects.equals(editImage, "null") && !Objects.equals(editImage, "") && !Objects.equals(editImage, null)) {
                        try {
                            Glide.with(getApplication().getApplicationContext()).load(editImage).into(imageIv);
                        }
                        catch (Exception ignored) {

                        }
                    } else if (editVideo != null && !Objects.equals(editVideo, "") && !Objects.equals(editVideo, "null")) {

                        try {
                            long thumb = 1000L;
                            RequestOptions options = new RequestOptions().frame(thumb);
                            Glide.with(getApplication().getApplicationContext()).load(editVideo).apply(options).fitCenter().centerCrop().into(imageIv);
                            playBtn.setVisibility(View.VISIBLE);

                            playBtn.setOnClickListener(view -> {
                                Intent intent = new Intent(AddPostActivity.this, VideoPlayerActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("videoUrl", editVideo);
                                getApplicationContext().startActivity(intent);
                            });

                        }
                        catch(NullPointerException e) {
                            Log.e("null thumbnail", String.valueOf(e));
                            Toast.makeText(AddPostActivity.this, "error"+e, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void uploadYoutubeEmbed(String title, String description, String youtubeUrl) {

        //for post-image name, post-id, post-publish-time

        final String timeStamp = String.valueOf(System.currentTimeMillis());

        String hotScore = String.valueOf(hot(Long.parseLong(timeStamp),1, Long.parseLong(timeStamp)));

              HashMap<Object, String> hashMap = new HashMap<>();
              //put post info
              hashMap.put("uid", uid);
              hashMap.put("uPseudonym", pseudonym);
              hashMap.put("uPractic", practic);
              hashMap.put("uDp", dp);
              hashMap.put("pId", timeStamp);
              hashMap.put("pTitle", title);
              hashMap.put("pLikes", pLikes);
              hashMap.put("hotScore", hotScore);
              hashMap.put("youtubeUrl", youtubeUrl);
              hashMap.put("pComments", pComments);
              hashMap.put("pDescr", description);
              hashMap.put("pImage", thumbnail);
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
                              //Toast.makeText(AddPostActivity.this, ""+R.string.publicado, Toast.LENGTH_SHORT).show();
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
                              Toast.makeText(AddPostActivity.this, "err"+e.getMessage(), Toast.LENGTH_SHORT).show();
                          }
                      });
    }

    private void uploadData(String title, String description) {

        //for post-image name, post-id, post-publish-time




        final String timeStamp = String.valueOf(System.currentTimeMillis());

        String hotScore = String.valueOf(hot(Long.parseLong(timeStamp),1, Long.parseLong(timeStamp)));
        String filePathAndName = "Posts/" + "post_" + timeStamp;
        if (imageIv.getDrawable() != null) {
            if (editImage != null) {
                StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
                mPictureRef.delete();
            }


            int bitSize = 3000000;
            int quality = 90;

            //get image from imageview
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] data = baos.toByteArray();
            Bitmap bitmap1 = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
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
            data = baos.toByteArray();



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
                                hashMap.put("hotScore", hotScore);
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
                                                //Toast.makeText(AddPostActivity.this, ""+R.string.publicado, Toast.LENGTH_SHORT).show();
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
                                                Toast.makeText(AddPostActivity.this, "err"+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        } else if (audioPath != null) {
            uploadAud(title, description);
        }

        else {
            //post without image
            HashMap<Object, String> hashMap = new HashMap<>();
            //put post info
            hashMap.put("uid", uid);
            hashMap.put("uPseudonym", pseudonym);
            hashMap.put("uPractic", practic);
            hashMap.put("pLikes", pLikes);
            hashMap.put("pDislikes", pDislikes);
            hashMap.put("pComments", pComments);
            hashMap.put("hotScore", hotScore);
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
                            //Toast.makeText(AddPostActivity.this, R.string.publicado, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(AddPostActivity.this, "err"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private long hot(long postDate, long likes, long currentDate) {
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeZone(TimeZone.getTimeZone("GMT"));
        int year = cal2.get(Calendar.YEAR);
        int yearNumber = (Math.abs(year) % 10)*10000;

        return (((currentDate - postDate) / 86400000) * 1000 - score(likes) - yearNumber * 10000)*(-1);
    }

    private long score(long likes) {
        return likes * 1000;
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
            Toast.makeText(this, "err"+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(AddPostActivity.this, "err"+error.toString(), Toast.LENGTH_SHORT).show();
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
       /* if(Util.SDK_INT >= 24){
            initPlayer();}
        checkUserStatus();*/
    }

    @Override
    protected void onStop() {
        if(Util.SDK_INT >= 24){
            releasePlayer();}
        super.onStop();
    }

    private void releasePlayer() {
        if(simpleExoPlayer != null){
            playWhenReady = simpleExoPlayer.getPlayWhenReady();
            playbackPosition = simpleExoPlayer.getCurrentPosition();
            currentWindow = simpleExoPlayer.getCurrentWindowIndex();
            simpleExoPlayer.release();
            simpleExoPlayer = null;
        }
    }

    private void initPlayer() {
        simpleExoPlayer = new SimpleExoPlayer.Builder(this).build();
        player.setPlayer(simpleExoPlayer);
        embedYoutube("https://www.youtube.com/watch?v=Gkhnwq7npM8");
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
      /*  if ((Util.SDK_INT < 24 || simpleExoPlayer == null)) {
            initPlayer();
        }*/
    }

    @Override
    protected void onPause() {
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
        super.onPause();
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
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
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
                Glide.with(this).load(resultUri).into(imageIv);
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
            else if (requestCode == PICK_AUD_REQUEST && data != null && data.getData() != null) {
                //Toast.makeText(this, "Got data", Toast.LENGTH_SHORT).show();
                audioPath = data.getData();
                imageIv.setVisibility(View.GONE);
                playerView.setVisibility(View.VISIBLE);

                simpleExoPlayer = new SimpleExoPlayer.Builder(AddPostActivity.this)
                        .setSeekBackIncrementMs(5000)
                        .setSeekForwardIncrementMs(5000)
                        .build();
                playerView.setPlayer(simpleExoPlayer);
                playerView.setKeepScreenOn(true);
                playerView.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 40);
                    }
                });

                playerView.setClipToOutline(true);
                mediaItem = MediaItem.fromUri(audioPath);
                simpleExoPlayer.setMediaItem(mediaItem);
                simpleExoPlayer.prepare();
                simpleExoPlayer.setPlayWhenReady(false);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadAud(String title, String description) {
        //upload audio to firebase storage
        if (audioPath != null) {
            final ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Uploading");
            pd.show();
            String timeStamp = "" + System.currentTimeMillis();
            String hotScore = String.valueOf(hot(Long.parseLong(timeStamp),1, Long.parseLong(timeStamp)));
            String filePathAndName = "Posts/" + "post_" + timeStamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(audioPath)
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
                                hashMap.put("hotScore", hotScore);
                                hashMap.put("pComments", pComments);
                                hashMap.put("pDescr", description);
                                hashMap.put("pAudio", downloadUri);
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
                                                //Toast.makeText(AddPostActivity.this, ""+R.string.publicado, Toast.LENGTH_SHORT).show();
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
                                                Toast.makeText(AddPostActivity.this, "err"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }

                        }

                    });
        }
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
            String hotScore = String.valueOf(hot(Long.parseLong(timestamp),1, Long.parseLong(timestamp)));
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

            storageReference.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        //video is uploaded to firebase storage, now get it's url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
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
                            hashMap.put("pDislikes", pDislikes);
                            hashMap.put("pComments", pComments);
                            hashMap.put("pDescr", description);
                            hashMap.put("hotScore", hotScore);
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
                                            //Toast.makeText(AddPostActivity.this, ""+R.string.publicado, Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(AddPostActivity.this, "err"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }

                    })
                    .addOnFailureListener(e -> {
                        pd.dismiss();
                        //failed adding post in storage
                        Log.e("YogaNetwork", "failed uploading post to database", e);
                        Toast.makeText(AddPostActivity.this, "err"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    });


        }
    }
}

