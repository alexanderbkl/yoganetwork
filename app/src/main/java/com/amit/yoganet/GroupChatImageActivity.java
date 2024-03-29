package com.amit.yoganet;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Locale;

public class GroupChatImageActivity extends AppCompatActivity {
    private ImageView imageIv;
    private String uri, senderUid;
    private Toolbar toolbar;
    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_image);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        imageIv = findViewById(R.id.imageIv);

        Intent intent = getIntent();
        uri = intent.getStringExtra("message");
        senderUid = intent.getStringExtra("senderUid");
        System.out.println(uri);
        try {
            Picasso.get().load(uri).into(imageIv);
        } catch (Exception e) {
            imageIv.setImageResource(R.drawable.ic_group_primary);
        }




        imageIv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {



                DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

                Long ts = System.currentTimeMillis();

                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                cal.setTimeInMillis(ts * 1000L);
                String date = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();

                Intent intent = getIntent();

                String rui = intent.getStringExtra("message");

                Uri uri = Uri.parse(rui);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setTitle("group "+senderUid+" "+date);
                request.setDescription("Descargando");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"Grupos Yoga");

                downloadmanager.enqueue(request);
                return false;

            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                finish();
            }

        });
    }
    
}