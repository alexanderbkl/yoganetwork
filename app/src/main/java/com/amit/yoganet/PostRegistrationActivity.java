package com.amit.yoganet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.BitmapCompat;
import com.amit.yoganet.crop.CropImage;
import com.amit.yoganet.crop.CropImageView;
import com.amit.yoganet.utils.ImageUtils;
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
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class PostRegistrationActivity extends AppCompatActivity {

   //views
    EditText pseudonymEt, nameEt, typeEt, practicEt, dietEt, descriptionEt;
    AppCompatButton updateBtn;
    ImageView coverIv, addCoverBtn, avatarIv;
    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    //storage
    StorageReference storageReference;
    //path where images of user profile and cover will be stored
    String storagePath = "Users_Profile_Cover_Imgs/", profileOrCoverPhoto, image;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_registration);
        pseudonymEt = findViewById(R.id.pseudonymEt);
        nameEt = findViewById(R.id.nameEt);
        typeEt = findViewById(R.id.typeEt);
        practicEt = findViewById(R.id.practicEt);
        dietEt = findViewById(R.id.dietEt);
        updateBtn = findViewById(R.id.updateBtn);
        descriptionEt = findViewById(R.id.descriptionEt);
        descriptionEt.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source != null) {
                    String s = source.toString();
                    if (s.contains("\n\n")) {
                        return s.replaceAll("\n\n", "\n");
                    }
                }
                return null;
            }
        }});
        coverIv = findViewById(R.id.coverIv);
        addCoverBtn = findViewById(R.id.addCoverBtn);
        avatarIv = findViewById(R.id.avatarIv);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
        pd = new ProgressDialog(PostRegistrationActivity.this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        //actionbar and its properties
        getSupportActionBar().setTitle(getString(R.string.profile_info));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Query query = databaseReference.orderByChild("uid").equalTo(user.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    String pseudonym = ""+ ds.child("pseudonym").getValue();
                    String realname = ""+ds.child("realname").getValue();
                    String type = ""+ ds.child("type").getValue();
                    String practic = ""+ ds.child("practic").getValue();
                    String diet = ""+ ds.child("diet").getValue();
                    image = ""+ ds.child("image").getValue();
                    String cover = ""+ ds.child("cover").getValue();
                    String description = ""+ ds.child("description").getValue();

                    //set data
                    pseudonymEt.setText(pseudonym);
                    nameEt.setText(realname);
                    typeEt.setText(type);
                    practicEt.setText(practic);
                    dietEt.setText(diet);
                    descriptionEt.setText(description);
                    try {
                        //if image is received then set
                        Glide.with(getApplication().getApplicationContext()).load(image).fitCenter().centerCrop().into(avatarIv);
                    }
                    catch (Exception e) {
                        //if there is any exception while getting image then set default
                    }
                    try {
                        //if image is received then set
                        Picasso.get().load(cover).into(coverIv);
                    }
                    catch (Exception e) {
                        //if there is any exception while getting image then set default
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        avatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd.setMessage("Actualizando foto de perfil");
                profileOrCoverPhoto = "image";
                startProfileCropActivity();
            }
        });

        coverIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd.setMessage("Actualizando cubierta");
                profileOrCoverPhoto = "cover";
                startCoverCropActivity();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadProfileData();
                /*Intent intent = new Intent(PostRegistrationActivity.this,DashboardActivity.class);
                intent.putExtra("fragmentPos","1");
                startActivity(intent);*/
            }
        });


        hideSystemNavBar();
        checkUserStatus();

    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
            String uid = user.getUid();
        }
        else {
            //user not signed in, go to mainactivity
            startActivity(new Intent(PostRegistrationActivity.this, MainActivity.class));
            this.finish();
        }
    }

    private void startCoverCropActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(41,15)
                .start(this);
    }


    private void startProfileCropActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this);
    }

    private void uploadProfileData() {
        String description = String.valueOf(descriptionEt.getText().toString().trim());
        if (descriptionEt.getText().toString().contains("\n\n\n") || descriptionEt.getText().toString().contains("\n\n\n\n") || descriptionEt.getText().toString().contains("\n\n\n\n\n")) {
            description = descriptionEt.getText().toString().replace("\n\n\n\n\n", "\n");
            description = description.replace("\n\n\n\n", "\n");
            description = description.replace("\n\n\n", "\n");
        }



            final String[] entrada = new String[]{"nombre espiritual", "nombre", "camino espiritual", "tipo de práctica", "alimentación", "descripción"};
            final String[] keys = new String[]{"pseudonym", "realname","practic", "type", "diet", "description"};
            final String[] entries = new String[6];
                 entries[0] = pseudonymEt.getText().toString().trim();
                 entries[1] = nameEt.getText().toString().trim();
                 entries[2] = practicEt.getText().toString().trim();
                 entries[3] = typeEt.getText().toString().trim();
                 entries[4] = dietEt.getText().toString().trim();
                 entries[5] = description;

        HashMap<String, Object> result = new HashMap<>();

        for (int i = 0; i<= 5; i++) {
                result.put(keys[i], entries[i]);
        }

        if (!TextUtils.isEmpty(entries[0])&&!TextUtils.isEmpty(entries[1])&&!TextUtils.isEmpty(entries[2])&&!TextUtils.isEmpty(entries[3])&&!TextUtils.isEmpty(entries[4])) {
            pd.show();

            firebaseDatabase = FirebaseDatabase.getInstance();

            databaseReference = firebaseDatabase.getReference("Users").child(user.getUid());
            //Toast.makeText(this, ""+user.getUid(), Toast.LENGTH_SHORT).show();

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    snapshot.child("pseudonym").getRef().setValue(entries[0]);
                    snapshot.child("realname").getRef().setValue(entries[1]);
                    snapshot.child("practic").getRef().setValue(entries[2]);
                    snapshot.child("type").getRef().setValue(entries[3]);
                    snapshot.child("diet").getRef().setValue(entries[4]);
                    snapshot.child("description").getRef().setValue(entries[5]);



                    pd.dismiss();
                    Toast.makeText(PostRegistrationActivity.this, getString(R.string.done)+"1", Toast.LENGTH_SHORT).show();
                    PostRegistrationActivity.this.finish();
                    Intent intent = new Intent(PostRegistrationActivity.this,DashboardActivity.class);
                    intent.putExtra("fragPos","1");
                    startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        //failed updating progress. dismiss progress and show error message
                        pd.dismiss();
                        Toast.makeText(PostRegistrationActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();

                    }

            });





        } else{
            Toast.makeText(PostRegistrationActivity.this, getString(R.string.introduce), Toast.LENGTH_SHORT).show();
        }
    }

    private void hideSystemNavBar() {
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (image != null && !image.equals("") && !image.equals("null")) {
                    System.out.println("image: " + image);
                    StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(image);
                    mPictureRef.delete();
                }

                Uri resultUri = result.getUri();
                try {
                    uploadProfileCoverPhoto(resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, ""+error, Toast.LENGTH_SHORT).show();
            }
        }
        try {
            super.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadProfileCoverPhoto(final Uri uri) throws IOException {
        //show progress
        pd.show();
        /*Instead of creating separate function for profile picture and cover photo this will work in the same function*/
        if (profileOrCoverPhoto == null){
            profileOrCoverPhoto = "cover";
        }
        //path and name of image to be stored in firebase storage
        String filePathAndName = storagePath+ ""+ profileOrCoverPhoto +"_"+ user.getUid()+".jpeg";
        String filePathAndNameFull = storagePath+ ""+ profileOrCoverPhoto +"_"+ user.getUid()+"Full.jpeg";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //get bitmap of uri
        Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        Bitmap bitmap2;
        byte[] data;
        int n = 1;
        int bitSize;
        int quality;
        if (profileOrCoverPhoto.equals("image")) {
            bitSize = 100000;
            quality = 70;
        } else {
            bitSize = 1000000;
            quality = 70;
        }

        do {
            bitmap2 = new ImageUtils().getResizedBitmap(bitmap1,bitmap1.getWidth()/n,bitmap1.getHeight()/n);
            if (n % 2 != 0) {
                n++;
            } else {
                n+=2;
            }
        } while (BitmapCompat.getAllocationByteCount(bitmap2) > bitSize);
        bitmap2.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        data = baos.toByteArray();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded to storage, now get its url and store in users database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        final Uri downloadUri = uriTask.getResult();

                        //check if image is uploading or not and url received
                        if (uriTask.isSuccessful()){
                            //image uploaded
                            //add/update url in users database
                            HashMap<String, Object> results = new HashMap<>();
                            /*first parameter is profileorcover photo thas has value "image" or "cover" which are keys in users database where url of the image
                             * be saved in of them
                             * Second parameter contains the url of the image stored in firebase storage, this url will be saved as value against key "image" or "cover"*/


                            databaseReference = firebaseDatabase.getReference("Users");

                            databaseReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if (profileOrCoverPhoto.equals("image")) {
                                        snapshot.child("image").getRef().setValue(downloadUri.toString());
                                    } else {
                                        snapshot.child("cover").getRef().setValue(downloadUri.toString());
                                    }
                                    pd.dismiss();
                                    Toast.makeText(PostRegistrationActivity.this, getString(R.string.done)+"2", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            });

                            //if user edit his name, also change it from hist posts
                            if (profileOrCoverPhoto.equals("image")){

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(user.getUid());
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                                            String child = ds.getKey();
                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                //update user image in current users comments on posts
                                ref.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                                            String child = ds.getKey();
                                            if (dataSnapshot.child(child).hasChild("Comments")){
                                                String child1 = ""+dataSnapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild(user.getUid()).equalTo(user.getUid());
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                                                            String child = ds.getKey();
                                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        }
                        else{
                            //error
                            pd.dismiss();
                            Toast.makeText(PostRegistrationActivity.this, "An error has occurred.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //there were some error, get and show error message, dismiss progress dialog
                        pd.dismiss();
                        Toast.makeText(PostRegistrationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        Bitmap bitmap3 = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

        bitmap3.compress(Bitmap.CompressFormat.JPEG, 80, baos);

        StorageReference storageReference3rd = storageReference.child(filePathAndNameFull);
        storageReference3rd.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded to storage, now get its url and store in users database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        final Uri downloadUri = uriTask.getResult();

                        //check if image is uploading or not and url received
                        if (uriTask.isSuccessful()){
                            //image uploaded
                            //add/update url in users database
                            /*first parameter is profileorcover photo thas has value "image" or "cover" which are keys in users database where url of the image
                             * be saved in of them
                             * Second parameter contains the url of the image stored in firebase storage, this url will be saved as value against key "image" or "cover"*/




                            firebaseDatabase = FirebaseDatabase.getInstance();

                            databaseReference = firebaseDatabase.getReference("Users").child(user.getUid());
                            //Toast.makeText(this, ""+user.getUid(), Toast.LENGTH_SHORT).show();

                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (profileOrCoverPhoto.equals("image")) {
                                        snapshot.child("imageFull").getRef().setValue(downloadUri.toString());
                                    }
                                    pd.dismiss();
                                    Toast.makeText(PostRegistrationActivity.this, getString(R.string.done)+"3", Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                    //failed updating progress. dismiss progress and show error message
                                    pd.dismiss();
                                    Toast.makeText(PostRegistrationActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();

                                }

                            });




                            //if user edit his name, also change it from hist posts
                            if (profileOrCoverPhoto.equals("image")){

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(user.getUid());
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                                            String child = ds.getKey();
                                            dataSnapshot.getRef().child(child).child("uDpFull").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        }
                        else{
                            //error
                            pd.dismiss();
                            Toast.makeText(PostRegistrationActivity.this, "An error has occurred.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //there were some error, get and show error message, dismiss progress dialog
                        pd.dismiss();
                        Toast.makeText(PostRegistrationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }
    @Override
    public boolean onSupportNavigateUp() {
        /*Intent intent = new Intent(PostRegistrationActivity.this,DashboardActivity.class);
        intent.putExtra("fragPos","2");
        startActivity(intent);*/
        PostRegistrationActivity.this.finish();
        return true;
    }

}