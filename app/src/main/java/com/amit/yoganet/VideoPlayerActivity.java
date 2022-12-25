package com.amit.yoganet;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

public class VideoPlayerActivity extends AppCompatActivity {

    ProgressBar progressBar;
    ImageView fullScreenBtn, lockBtn;
    PlayerView playerView;
    SimpleExoPlayer simpleExoPlayer;
    MediaItem mediaItem;
    String videoUrl;
    Uri videoSource;
    Boolean isFullScreen = false, isLock = false;
    LinearLayout sec_controlvid1, sec_controlvid2;
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        int UI_OPTIONS = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        getWindow().getDecorView().setSystemUiVisibility(UI_OPTIONS);
        playerView = findViewById(R.id.player);
        progressBar = findViewById(R.id.progress_bar);
        fullScreenBtn = findViewById(R.id.fullScreenBtn);
        lockBtn = findViewById(R.id.exo_lock);
        sec_controlvid1 = findViewById(R.id.sec_controlvid1);
        sec_controlvid2 = findViewById(R.id.sec_controlvid2);


            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                videoUrl= null;
            } else {
                videoUrl= extras.getString("videoUrl");
            }


        fullScreenBtn.setOnClickListener(view -> {
            if(!isFullScreen) {
                fullScreenBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.exo_icon_fullscreen_exit));
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
            else {
                fullScreenBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.exo_icon_fullscreen_enter));
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            isFullScreen = !isFullScreen;
        });

        lockBtn.setOnClickListener(view -> {
            if(!isLock)
            {
                lockBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_lock));
            }
            else {
                lockBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_lock_open));
            }
            isLock = !isLock;
            lockScreen(isLock);

        });

        simpleExoPlayer = new SimpleExoPlayer.Builder(this)
                .setSeekBackIncrementMs(5000)
                .setSeekForwardIncrementMs(5000)
                .build();
        playerView.setPlayer(simpleExoPlayer);
        playerView.setKeepScreenOn(true);
        simpleExoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if(playbackState == Player.STATE_BUFFERING)
                {
                    progressBar.setVisibility(View.VISIBLE);
                }
                else if (playbackState == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        videoSource = Uri.parse(videoUrl);
        mediaItem = MediaItem.fromUri(videoSource);
        simpleExoPlayer.setMediaItem(mediaItem);
        simpleExoPlayer.prepare();
        simpleExoPlayer.play();


    }

    private void lockScreen(Boolean isLock) {
        if(isLock)
        {

            sec_controlvid1.setVisibility(View.INVISIBLE);
            sec_controlvid2.setVisibility(View.INVISIBLE);
        } else {
            sec_controlvid1.setVisibility(View.VISIBLE);
            sec_controlvid2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if(isLock && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                fullScreenBtn.performClick();
            }

        else {super.onBackPressed();}
    }

    @Override
    protected void onStop() {
        super.onStop();
        simpleExoPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        simpleExoPlayer.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        simpleExoPlayer.pause();
    }



}