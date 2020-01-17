package com.cpacm.musicbtn;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cpacm.FloatingMusicMenu;
import com.cpacm.RotatingProgressDrawable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class MusicPlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private MediaPlayer mediaPlayer;
    private FloatingMusicMenu fmm;
    private TextView progressTv, totalTv;
    private SeekBar musicSeekBar;
    private FloatingActionButton playOrPauseFab, nextFab;
    private MusicHandler musicHandler;
    private boolean isPlaying = false;

    private ImageView rotIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicHandler = new MusicHandler();

        progressTv = findViewById(R.id.progress_tv);
        totalTv = findViewById(R.id.total_tv);
        musicSeekBar = findViewById(R.id.music_seekbar);
        fmm = findViewById(R.id.fmm);
        playOrPauseFab = findViewById(R.id.fab_play);
        nextFab = findViewById(R.id.fab_next);
        nextFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MusicPlayerActivity.this, FmmActivity.class));
            }
        });
        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int musicDuration = mediaPlayer.getDuration();
                if (musicDuration == -1) return;
                progressTv.setText(MusicUtils.getTime(progress));
                totalTv.setText(MusicUtils.getTime(musicDuration));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int musicDuration = mediaPlayer.getDuration();
                if (musicDuration == -1) return;
                int progress = seekBar.getProgress();
                mediaPlayer.seekTo(progress);
                mediaPlayer.start();
                progressTv.setText(MusicUtils.getTime(progress));
                totalTv.setText(MusicUtils.getTime(musicDuration));
            }
        });

        getSupportActionBar().setTitle("Music Player");
        initMusicPlayer();
    }

    public void initMusicPlayer() {
        musicPrepared();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        playOrPauseFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });
        fmm.setMusicCover(getResources().getDrawable(R.drawable.cover));

    }

    private void updateProgress() {
        int musicDuration = mediaPlayer.getDuration();
        if (musicDuration == -1) return;
        int musicProgress = mediaPlayer.getCurrentPosition();
        musicSeekBar.setProgress(musicProgress);
        musicSeekBar.setMax(musicDuration);
        fmm.setProgress(musicProgress * 100 / musicDuration);
    }

    private void musicPrepared() {
        try {
            AssetFileDescriptor afd = getAssets().openFd("99nights.mp3");
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer = null;
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toggle() {
        if (isPlaying) {
            pause();
        } else {
            start();
        }
    }

    public void start() {
        isPlaying = true;
        mediaPlayer.start();
        musicHandler.sendEmptyMessage(0);
        playOrPauseFab.setImageResource(R.drawable.ic_play);
        fmm.start();
    }

    public void pause() {
        isPlaying = false;
        mediaPlayer.pause();
        musicHandler.removeMessages(0);
        playOrPauseFab.setImageResource(R.drawable.ic_pause);
        fmm.stop();
    }

    public void complete() {
        isPlaying = false;
        mediaPlayer.stop();
        musicHandler.removeMessages(0);
        playOrPauseFab.setImageResource(R.drawable.ic_pause);
        fmm.stop();
        fmm.setProgress(100);
        musicPrepared();
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        complete();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("MMP", "Media player error: what=" + what + ", extra=" + extra);
        return false;
    }

    public class MusicHandler extends Handler {

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            updateProgress();
            sendEmptyMessageDelayed(0, 1000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicHandler.removeMessages(0);
    }
}
