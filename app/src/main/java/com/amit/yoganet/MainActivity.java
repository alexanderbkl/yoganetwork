package com.amit.yoganet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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


