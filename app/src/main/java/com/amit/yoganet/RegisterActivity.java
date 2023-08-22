package com.amit.yoganet;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;




public class RegisterActivity extends AppCompatActivity {
//views





    private EditText mEmailEt, mPasswordEt;
    private boolean acceptedTerms = false;
    private CheckBox userPolicy;
    //progressbar to display while registering user
    private ProgressDialog progressDialog;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
 //init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        Button mRegisterBtn = findViewById(R.id.registerBtn);
        ImageButton mGoogleLoginBtn = findViewById(R.id.googleLoginBtn);
        TextView mHaveAccountTv = findViewById(R.id.have_accountTv);
        TextView userPolicyText = findViewById(R.id.userPolicyText);
//        In the onCreate() method, initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.registrando));

        userPolicyText.setOnClickListener(v -> {
            AlertDialog.Builder userPolicyDialog = new AlertDialog.Builder(RegisterActivity.this);
            userPolicyDialog.setTitle(R.string.user_policy_title);
            userPolicyDialog.setMessage(R.string.user_policy_message);
            userPolicyDialog.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Handle acceptance of user policy
                    acceptedTerms = true;
                    userPolicy.setChecked(true);
                }
            });
            userPolicyDialog.setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Handle decline of user policy
                    acceptedTerms = false;
                    userPolicy.setChecked(false);
                }
            });
            userPolicyDialog.show();

        });

        //handle checkbox click
        userPolicy = findViewById(R.id.userPolicy);
        userPolicy.setOnClickListener(v -> {
            if (userPolicy.isChecked()) {
                acceptedTerms = true;
            } else {
                acceptedTerms = false;
            }
        });

        //handle register btn click
        mRegisterBtn.setOnClickListener(v -> {


            if (acceptedTerms) {
                //input email, password
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                //validate
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //set error and focus to email edittext
                    mEmailEt.setError(getString(R.string.emailinvalid));
                    mEmailEt.setFocusable(true);
                }
                else if (password.length()<6) {
                    //set error and focus to password edittext
                    mPasswordEt.setError(getString(R.string.menor));
                    mPasswordEt.setFocusable(true);
                }
                else {
                    //valid email pattern
                    registerUser (email, password);
                }
            } else {
                // User declined user policy
                Toast.makeText(this, getString(R.string.accept_policy), Toast.LENGTH_SHORT).show();
            }

        });
        //handle login textview click listener
        mHaveAccountTv.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
        mGoogleLoginBtn.setOnClickListener(v -> {
            if (acceptedTerms) {
                // Handle Google login
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            } else {
                // User declined user policy
                Toast.makeText(this, getString(R.string.accept_policy), Toast.LENGTH_SHORT).show();
            }
        });
        //hmm2
    }
//alexhequitado lo de regem regpas
    private void registerUser(String email, String password) {
        //email and password pattern is valid, show progress dialog and start registering user
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {


                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, dismiss dialog and start register activity
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();

                            //get user email and uid from auth
                            String email = user.getEmail();
                            String uid = user.getUid();
                            //Data will be saved in "users" node.
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("pseudonym", "");
                            hashMap.put("image", String.valueOf(user.getPhotoUrl()));
                            hashMap.put("onlineStatus", "Online");
                            hashMap.put("typingTo", "noOne");
                            hashMap.put("type", "");
                            hashMap.put("practic", "");
                            hashMap.put("purpose", "");
                            hashMap.put("country", "");
                            hashMap.put("city", "");
                            hashMap.put("diet", "");
                            hashMap.put("cover", "");
                            hashMap.put("description","");
                            hashMap.put("userLikes","0");

                            //firebase database instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //path to store user data named users
                            //put data within hashmap in database
                            database.getReference("Users").child(uid).setValue(hashMap);

                            Toast.makeText(RegisterActivity.this, getString(R.string.registrado)+user.getEmail(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, PostRegistrationActivity.class));
                        finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, R.string.fallida, Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error, dismiss progress dialog and get and show the error message
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go previous activity
        return super.onSupportNavigateUp();
    }
}