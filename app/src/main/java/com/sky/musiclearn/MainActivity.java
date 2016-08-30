package com.sky.musiclearn;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ImageView;

import com.sky.musiclearn.service.MidiSynthDeviceService;
import com.sky.musiclearn.studymodel.VisualizerView;
import com.sky.musiclearn.ui.MainLayout;

public class MainActivity extends Activity {
    String TAG = "MIDI";
    MainLayout mainLayout;
    ImageView studyModel;
    ImageView teachModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainLayout = new MainLayout(this);
        setContentView(mainLayout);
    }

}
