package com.android.yoganetwork;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.yoganetwork.fragments.MapFragment;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
//views
  private Button mRegisterBtn, mLoginBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init views
        mRegisterBtn = (Button) findViewById(R.id.register_btn);
        mLoginBtn = findViewById(R.id.login_btn);


        //handle register button click
        //hmm
        mRegisterBtn.setOnClickListener(v -> {
//start RegisterActivity



            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            });
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start LoginActivity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });


        };
        //handle login button click



}


