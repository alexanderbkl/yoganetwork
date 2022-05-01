package com.android.yoganetwork;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.graphics.BitmapCompat;
import com.android.yoganetwork.utils.ImageUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.android.yoganetwork.crop.CropImageActivity;
import com.android.yoganetwork.crop.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class GroupEditActivity extends AppCompatActivity {

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;
    //picked image uri
    private Uri image_uri = null;

    private ActionBar actionBar;

    private ProgressDialog progressDialog;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //UI views
    private ImageView groupIconIv;
    private EditText groupTitleEt, groupDescriptionEt;
    private FloatingActionButton updateGroupBtn;

    private String groupId, groupIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);
        setSupportActionBar(findViewById(R.id.toolbar));
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.editargrupo);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //init UI views
        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitleEt = findViewById(R.id.groupTitleEt);
        groupDescriptionEt = findViewById(R.id.groupDescriptionEt);
        updateGroupBtn = findViewById(R.id.updateGroupBtn);

        groupId = getIntent().getStringExtra("groupId");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.espere));
        progressDialog.setCanceledOnTouchOutside(true);

        //init permission arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadGroupInfo();

        //pick image
        groupIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(GroupEditActivity.this);
            }
        });

        //handle click event
        updateGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startUpdatingGroup();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void startUpdatingGroup() throws IOException {
        //input data
        String groupTitle = groupTitleEt.getText().toString().trim();
        String groupDescription = groupDescriptionEt.getText().toString().trim();
        //validate data
        if (TextUtils.isEmpty(groupTitle)) {
            Toast.makeText(this, R.string.titulovacio, Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage(getString(R.string.actualizando));
        progressDialog.show();
        
        if (image_uri == null) {
            //update group without icon


            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupTitle",groupTitle);
            hashMap.put("groupDescription",groupDescription);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
            ref.child(groupId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(GroupEditActivity.this, R.string.actualizado1, Toast.LENGTH_SHORT).show();

                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(GroupEditActivity.this, "Error "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            //update group with icon

            String timestamp = ""+System.currentTimeMillis();

            String filePathAndName = "Group_Imgs/"+"image"+"_"+timestamp;

            //upload image to firebase storage
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

            int bitSize = 3000000;
            int quality = 60;

            //get image from imageview
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] data = baos.toByteArray();
            //image uri to bitmap

            Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);

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
            if (groupIcon != null && !groupIcon.equals("")) {
                StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(groupIcon);
                mPictureRef.delete();
            }


            storageReference.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //uploaded image
                            //get url
                            Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!p_uriTask.isSuccessful());
                            Uri p_downloadUri = p_uriTask.getResult();
                            if (p_uriTask.isSuccessful()) {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("groupTitle",groupTitle);
                                hashMap.put("groupDescription",groupDescription);
                                hashMap.put("groupIcon",""+p_downloadUri);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                                ref.child(groupId).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                Toast.makeText(GroupEditActivity.this, R.string.actualizado1, Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(GroupEditActivity.this, "Error "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                       //upload failed
                       progressDialog.dismiss();
                            Toast.makeText(GroupEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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
                    groupIcon = ""+ds.child("groupIcon").getValue();
                    String createdBy = ""+ds.child("createdBy").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();

                    //convert timestamp to dd/mm/yyyy hh:mm am/pm
                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(Long.parseLong(timestamp));
                    String dateTime = DateFormat.format("dd/MM HH:mm", cal).toString();


                    //set group info
                    groupTitleEt.setText(groupTitle);
                    groupDescriptionEt.setText(groupDescription);
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

    private void showImagePickDialog() {
        //options to pick image from
        String[] options = {getString(R.string.camera), getString(R.string.galeria)};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.escogerimg)
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle clicks

                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .start(GroupEditActivity.this);
                    }
                }).show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/+");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Group Image Icon Title");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Group Image Icon Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }



    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
        }
    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //handle permission result
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //permission allowed
                        pickFromCamera();
                    } else {
                        //both or one is denied
                        Toast.makeText(this, "Camera & Storage permissions are required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //permission allowed
                        pickFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Storage permissions required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //handle image pick result

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                groupIconIv.setImageURI(resultUri);
                image_uri = resultUri;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                System.out.println(error);
            }
        }



        super.onActivityResult(requestCode, resultCode, data);
    }
}