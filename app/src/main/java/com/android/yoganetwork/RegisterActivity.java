package com.android.yoganetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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





    EditText mEmailEt, mPasswordEt;
    Button mRegisterBtn;
    ImageButton mGoogleLoginBtn;
    TextView mHaveAccountTv;
    //progressbar to display while registering user
    ProgressDialog progressDialog;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //Actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.crearcuenta);
        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
 //init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mGoogleLoginBtn = findViewById(R.id.googleLoginBtn);
        mHaveAccountTv = findViewById(R.id.have_accountTv);
//        In the onCreate() method, initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.registrando));
        //hmm1
        //google btn

        //handle register btn click
        mRegisterBtn.setOnClickListener(v -> {
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
        });
        //handle login textview click listener
        mHaveAccountTv.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
        mGoogleLoginBtn.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
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
                            hashMap.put("image", "");
                            hashMap.put("realname", "");
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("typingTo", "noOne");
                            hashMap.put("type", "");
                            hashMap.put("practic", "");
                            hashMap.put("diet", "");
                            hashMap.put("cover", "");

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
                Toast.makeText(RegisterActivity.this, getString(R.string.bienvenido)+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go previous activity
        return super.onSupportNavigateUp();
    }
}