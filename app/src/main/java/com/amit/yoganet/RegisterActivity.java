package com.amit.yoganet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
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
import java.util.Locale;


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
        Button changeLang = findViewById(com.amit.yoganet.R.id.changeMyLang);
        loadLocale();

        String lang = Locale.getDefault().getLanguage();

        switch (lang) {
            case "es":
                changeLang.setText("CAMBIAR\nIDIOMA\n(Español)");
                break;
            case "en":
                changeLang.setText("CHANGE\nLANGUAGE\n(English)");
                break;
            case "fr":
                changeLang.setText("CHANGER\nLANGUE\n(Français)");
                break;
            case "de":
                changeLang.setText("SPRACHE\nÄNDERN\n(Deutsch)");
                break;
            case "ar":
                changeLang.setText("غير اللغة\n(العربية)");
                break;
            case "hi":
                changeLang.setText("भाषा बदलें\n(हिन्दी)");
                break;
            case "ru":
                changeLang.setText("Изменить\nязык\n(Русский)");
                break;
            default:
                changeLang.setText("CHANGE\nLANGUAGE");
                break;
        }
        changeLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show AlertDialog to display list of languages, one can be selected
                showChangeLanguageDialog();
            }
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

    private void showChangeLanguageDialog() {
        final String[] listItems = {"English", "Español", "Русский", "Français", "Deutsche", "عربى", "हिन्दी"};
        androidx.appcompat.app.AlertDialog.Builder mBuilder = new androidx.appcompat.app.AlertDialog.Builder(RegisterActivity.this);
        mBuilder.setTitle(com.amit.yoganet.R.string.lngchoose);
        mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    setLocale("en");
                    recreate();
                }  if (which == 1) {
                    setLocale("es");
                    recreate();
                }  if (which == 2) {
                    setLocale("ru");
                    recreate();
                }  if (which == 3) {
                    setLocale("fr");
                    recreate();
                }  if (which == 4) {
                    setLocale("de");
                    recreate();
                }  if (which == 5) {
                    setLocale("ar");
                    recreate();
                }  if (which == 6) {
                    setLocale("hi");
                    recreate();
                }

                //dismiss alert dialog when language selected
                dialog.dismiss();

            }
        });

        androidx.appcompat.app.AlertDialog mDialog = mBuilder.create();
        //show alert dialog
        mDialog.show();

    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        //save data to shared preferences
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();

    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go previous activity
        return super.onSupportNavigateUp();
    }

    public void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        setLocale(language);
    }
}