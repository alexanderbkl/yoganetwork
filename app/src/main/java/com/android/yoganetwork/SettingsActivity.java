package com.android.yoganetwork;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.messaging.FirebaseMessaging;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    //init views
    SwitchCompat postSwitch;

    //use shared preferences to save the state of Switch
    SharedPreferences sp;
    SharedPreferences.Editor editor; //to edit value of shared pref

    //constant for topic
    private static final String TOPIC_POST_NOTIFICATION = "POST"; //assing any value but use same for this kind of notification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        setSupportActionBar(findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.ajustes);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        postSwitch = findViewById(R.id.postSwitch);

        Button changeLang = findViewById(R.id.changeMyLang);
        String lang = Locale.getDefault().getLanguage();

        switch (lang) {
            case "es":
                changeLang.setText("CAMBIAR IDIOMA\n(Español)");
                break;
            case "en":
                changeLang.setText("CHANGE LANGUAGE\n(English)");
                break;
            case "fr":
                changeLang.setText("CHANGER LANGUE\n(Français)");
                break;
            case "de":
                changeLang.setText("SPRACHE ÄNDERN\n(Deutsch)");
                break;
            case "ar":
                changeLang.setText("غير اللغة\n(العربية)");
                break;
            case "hi":
                changeLang.setText("भाषा बदलें\n(हिन्दी)");
                break;
            case "ru":
                changeLang.setText("Изменить язык\n(Русский)");
                break;
            default:
                changeLang.setText("CHANGE LANGUAGE");
                break;
        }
        changeLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show AlertDialog to display list of languages, one can be selected
                showChangeLanguageDialog();
            }
        });

        Button deleteAcc = findViewById(R.id.deleteAcc);
        deleteAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show AlertDialog to display delete account
                showDeleteAccountDialog();
            }
        });



        loadLocale();

        //change actionbar title, if you dont change it will be according to your systems default language


        //init sp

        sp = getSharedPreferences("Notification_SP", MODE_PRIVATE);
        boolean isPostEnabled = sp.getBoolean(""+TOPIC_POST_NOTIFICATION, true);
        //if enabled check swithch, otherwise uncheck switch - by default unchecked/false
        if (isPostEnabled) {
            postSwitch.setChecked(true);
        } else {
            postSwitch.setChecked(false);
        }
        //implement switch change listener
        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //edit switch state
                editor = sp.edit();
                editor.putBoolean(""+TOPIC_POST_NOTIFICATION, isChecked);
                editor.apply();
                
                if (isChecked) {
                    subscribePostNotification();
                }
                else {
                    unsubscrubePostNotification();
                }
            }
        });
    }

    private void showDeleteAccountDialog() {
        //show delete message confirm dialog

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle(R.string.eliminar);
        builder.setMessage("Estás seguro de que quieres eliminar tu cuenta?");
        //delete button
        builder.setPositiveButton(R.string.eliminar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                deleteAcc();
            }
        });
        //cancel delete button
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss dialog
                dialog.dismiss();
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void deleteAcc() {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        //delete every post of this user from Posts
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        //get detail of post using id of post
        Query query = reference.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //delete post
                    ds.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //delete user from Users
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
        reference1.child(uid).removeValue();
        //delete user from FirebaseAuth
        FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                    //delete user from shared preferences
                    sp = getSharedPreferences("User_SP", MODE_PRIVATE);
                    editor = sp.edit();
                    editor.clear();
                    editor.apply();
                    FirebaseAuth.getInstance().signOut();
                    //delete user from FirebaseMessaging
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(uid).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(SettingsActivity.this, "Cuenta eliminada", Toast.LENGTH_SHORT).show();
                            //go to login activity
                            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                            finish();
                        }
                    });

            }
        });

    }

    private void showChangeLanguageDialog() {
        final String[] listItems = {"English", "Español", "Русский", "Français", "Deutsche", "عربى", "हिन्दी"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(SettingsActivity.this);
        mBuilder.setTitle(R.string.lngchoose);
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

        AlertDialog mDialog = mBuilder.create();
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
    //load language saved in shared preferences
    public void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        setLocale(language);
    }

    private void unsubscrubePostNotification() {
        //unsubscribe to a topic (POST) to disable its notifications
        FirebaseMessaging.getInstance().unsubscribeFromTopic(""+TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.nonotif);
                        if (!task.isSuccessful()) {
                            msg = "o desubscripçao faió";
                        }
                        Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    //now, in AddPostActivity when user publish post send notification with same topic "POST"
    private void subscribePostNotification() {
        //subscribe to a topic (POST) to enable its notifications
        FirebaseMessaging.getInstance().subscribeToTopic(""+TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    String msg = getString(R.string.notif);
                    if (!task.isSuccessful()) {
                        msg = "o subscripçao faió";
                    }
                        Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();

    }
}