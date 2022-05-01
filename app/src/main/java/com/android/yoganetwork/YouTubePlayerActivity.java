package com.android.yoganetwork;

import android.annotation.SuppressLint;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.yoganetwork.youtubeExtractor.VideoMeta;
import com.android.yoganetwork.youtubeExtractor.YouTubeExtractor;
import com.android.yoganetwork.youtubeExtractor.YtFile;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.DefaultPlayerUiController;

import static com.android.yoganetwork.AddPostActivity.getYoutubeVideoId;

public class YouTubePlayerActivity extends AppCompatActivity {

    ProgressBar progressBar;
    ImageView fullScreenBtn, lockBtn;
    PlayerView playerView;
    SimpleExoPlayer player;
    String youtubeUrl;
    YouTubePlayerView youTubePlayerView;
    boolean playWhenReady = true;
    long playbackPosition = 0;
    int currentWindow = 0;
    Boolean isFullScreen = false, isLock = false;
    LinearLayout sec_controlvid1, sec_controlvid2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_you_tube_player);
        youTubePlayerView = findViewById(R.id.youtube_player_view);

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            youtubeUrl= null;
        } else {
            youtubeUrl= extras.getString("youtubeUrl");
        }
        int UI_OPTIONS = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        getWindow().getDecorView().setSystemUiVisibility(UI_OPTIONS);
        progressBar = findViewById(R.id.progress_bar);
        String videoId = getYoutubeVideoId(youtubeUrl);
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                                                       @Override
                                                       public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                                                           youTubePlayer.loadVideo(videoId, 0);
                                                       }
                                                   });

        youTubePlayerView.toggleFullScreen();

        YouTubePlayerListener listener = new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                // using pre-made custom ui
                DefaultPlayerUiController defaultPlayerUiController = new DefaultPlayerUiController(youTubePlayerView, youTubePlayer);
                youTubePlayerView.setCustomPlayerUi(defaultPlayerUiController.getRootView());
            }
        };

// disable iframe ui
        IFramePlayerOptions options = new IFramePlayerOptions.Builder().controls(0).build();
        youTubePlayerView.initialize(listener, options);


/*

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

        player = new SimpleExoPlayer.Builder(this)
                .setSeekBackIncrementMs(5000)
                .setSeekForwardIncrementMs(5000)
                .build();
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        playYouTubeVideo(youtubeUrl);
        player.addListener(new Player.Listener() {
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

*/


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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        youTubePlayerView.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void playYouTubeVideo(String youtubeUrl) {
        new YouTubeExtractor(this) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
                if (ytFiles != null) {
                    int videoTag = 137;
                    int audioTag = 140;
                    MediaSource audioSource, videoSource;
                    try {
                        audioSource = new ProgressiveMediaSource
                                .Factory(new DefaultHttpDataSource.Factory())
                                .createMediaSource(MediaItem.fromUri(ytFiles.get(audioTag).getUrl()));
                        videoSource = new ProgressiveMediaSource
                                .Factory(new DefaultHttpDataSource.Factory())
                                .createMediaSource(MediaItem.fromUri(ytFiles.get(videoTag).getUrl()));

                    } catch (Exception e) {
                        Toast.makeText(YouTubePlayerActivity.this, "error"+e, Toast.LENGTH_SHORT).show();
                        audioSource = new ProgressiveMediaSource
                                .Factory(new DefaultHttpDataSource.Factory())
                                .createMediaSource(MediaItem.fromUri(ytFiles.get(audioTag).getUrl()));
                        videoSource = new ProgressiveMediaSource
                                .Factory(new DefaultHttpDataSource.Factory())
                                .createMediaSource(MediaItem.fromUri(ytFiles.get(22).getUrl()));


                    }
                     player.setMediaSource(new MergingMediaSource(
                                    true,
                                    videoSource,
                                    audioSource),
                            true
                    );
                    player.prepare();
                    player.setPlayWhenReady(playWhenReady);
                    player.seekTo(currentWindow,playbackPosition);


                }
            }

        }.extract(youtubeUrl, false, true);
    }

}