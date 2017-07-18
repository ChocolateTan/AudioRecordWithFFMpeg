package com.orz.testrecord;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity {

  AudioRecorderUtil mAudioRecorderUtil;//= new AudioRecorderUtil()

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (PermissionsChecker.lacksPermissions(this, new String[]{
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    })) {
      ActivityCompat.requestPermissions(this, new String[]{
          Manifest.permission.READ_EXTERNAL_STORAGE,
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.RECORD_AUDIO
      }, 1);
    }

    findViewById(R.id.btn_start).setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(true);
        mAudioRecorderUtil = new AudioRecorderUtil(
            MainActivity.this,
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/donVoi/voice",
            System.currentTimeMillis() + ".pcm");
        mAudioRecorderUtil.prepare();
        mAudioRecorderUtil.start();
      }
    });
    findViewById(R.id.btn_pasue).setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        mAudioRecorderUtil.pause();
      }
    });
    findViewById(R.id.btn_resume).setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        mAudioRecorderUtil.resume();
      }
    });
    findViewById(R.id.btn_stop).setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        mAudioRecorderUtil.stop();
      }
    });
    findViewById(R.id.btn_play).setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        mAudioRecorderUtil.play();
      }
    });
  }
}
