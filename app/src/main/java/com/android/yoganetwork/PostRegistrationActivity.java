package com.android.yoganetwork;

import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.BitmapCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.yoganetwork.fragments.ProfileFragment;
import com.android.yoganetwork.utils.ImageUtils;
import com.bumptech.glide.Glide;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

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
    String storagePath = "Users_Profile_Cover_Imgs/", profileOrCoverPhoto;

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
        getSupportActionBar().setTitle("INFORMACIÓN DEL PERFIL");
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
                    String image = ""+ ds.child("image").getValue();
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
                        Glide.with(PostRegistrationActivity.this).load(image).fitCenter().centerCrop().into(avatarIv);
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
                Intent intent = new Intent(PostRegistrationActivity.this,DashboardActivity.class);
                intent.putExtra("fragmentPos","1");
                startActivity(intent);
                PostRegistrationActivity.this.finish();
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
            for (int i = 0; i<= 5; i++) {


            if (!TextUtils.isEmpty(entries[0])&&!TextUtils.isEmpty(entries[1])&&!TextUtils.isEmpty(entries[2])&&!TextUtils.isEmpty(entries[3])&&!TextUtils.isEmpty(entries[4])) {
                pd.show();
                HashMap<String, Object> result = new HashMap<>();
                result.put(keys[i], entries[i]);

                databaseReference.child(user.getUid()).updateChildren(result)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //updated, dismiss progress
                                pd.dismiss();
                                Toast.makeText(PostRegistrationActivity.this, "Hecho!", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //failed updating progress. dismiss progress and show error message
                                pd.dismiss();
                                Toast.makeText(PostRegistrationActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

        } else{
                Toast.makeText(PostRegistrationActivity.this, "Porfavor introduzca "+entrada[i], Toast.LENGTH_SHORT).show();
            }
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
                Uri resultUri = result.getUri();
                try {
                    uploadProfileCoverPhoto(resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                System.out.println(error);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(final Uri uri) throws IOException {
        //show progress
        pd.show();
        /*Instead of creating separate function for profile picture and cover photo this will work in the same function*/

        //path and name of image to be stored in firebase storage
        String filePathAndName = storagePath+ ""+ profileOrCoverPhoto +"_"+ user.getUid()+".jpeg";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //get bitmap of uri
        Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        Bitmap bitmap2;
        byte[] data;
        int n = 2;
        do {
            bitmap2 = new ImageUtils().getResizedBitmap(bitmap1,bitmap1.getWidth()/n,bitmap1.getHeight()/n);
            n+=2;
            System.out.println("Hola"+BitmapCompat.getAllocationByteCount(bitmap2));
        } while (BitmapCompat.getAllocationByteCount(bitmap2) > 30000);
        bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, baos);
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
                            results.put(profileOrCoverPhoto, downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //URL IN DATA BASE of user is add succesfully
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(PostRegistrationActivity.this, "Hecho!", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //error adding url in database of user
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(PostRegistrationActivity.this, "Error", Toast.LENGTH_SHORT).show();
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
    }
    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(PostRegistrationActivity.this,DashboardActivity.class);
        intent.putExtra("fragment",2);
        startActivity(intent);
        PostRegistrationActivity.this.finish();
        return true;
    }

}