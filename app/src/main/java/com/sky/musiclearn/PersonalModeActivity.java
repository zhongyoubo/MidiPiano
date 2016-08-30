package com.sky.musiclearn;

/**
 * Created by Sky000 on 2016/8/18.
 */

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
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sky.musiclearn.service.MidiSynthDeviceService;
import com.sky.musiclearn.studymodel.VisualizerView;
import com.sky.musiclearn.synth.MidiOutputPortConnectionSelector;
import com.sky.musiclearn.synth.MidiPortConnector;
import com.sky.musiclearn.synth.MidiTools;
import com.sky.musiclearn.ui.MidiPianoLayout;
import com.sky.musiclearn.utils.Debugger;
import com.sky.musiclearn.utils.MusicLearnConfig;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;


/**
 * Created by Sky000 on 2016/8/17.
 */
public class PersonalModeActivity extends Activity {
    private static final float VISUALIZER_HEIGHT_DIP = 150f;//频谱View高度
    private MediaPlayer mMediaPlayer;//音频
    private Visualizer mVisualizer;//频谱器
    private Equalizer mEqualizer; //均衡器
    VisualizerView visualizerView;
    MidiSynthDeviceService midiSynthDeviceService;
    AudioTrack audioTrack;
    Button startRecordBtn;
    Button stopRecordBtn;
    TextView startTip;
    TextView stopTip;
    TextView displayTv;
    private boolean isRecording = false;
    static byte[] data = new byte[256];
    static final String TAG = "MidiSynthExample";
    static MidiPianoLayout midiPianoLayout;
    private MidiManager mMidiManager;
    private MidiOutputPortConnectionSelector mPortSelector;
    private  static final int RECEIVE = 1;
    public  static Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case  RECEIVE:
                    data = (byte[])msg.obj;
                    int keyNum = data[1];     //代表那个按键被按下
                    int keyStatus = data[2];  //代表按键被按下的状态  127代表按键被按下   0 表示按键抬起
                    chageKeyBoard(keyNum,keyStatus);
                    Debugger.i("PersonalModeActivity","data[1]=="+data[1]+"data[2]=="+data[2]);
                    break;
                default:
                    break;
               }
            return false;
        }
    });


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        midiPianoLayout = new MidiPianoLayout(this);
        setContentView(midiPianoLayout);
        Debugger.i(TAG,"oncreate");
        Log.i(TAG,"setupMidi start");
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            setupMidi();
            Log.i(TAG,"setupMidi sucessfull");
        }
        Log.i(TAG,"setupMidi end");
        Debugger.i("PersonalModeActivity","Thread.currentThread()=="+Thread.currentThread());

        //动态开启RECORD_AUDIO权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);
        }

        startTip = midiPianoLayout.getStartTip();
        stopTip = midiPianoLayout.getStopTip();
        displayTv = midiPianoLayout.getDisplayTv();
        startRecordBtn = midiPianoLayout.getStartRecordBtn();
        startRecordBtn.setOnClickListener(new onStartClickListener());
        stopRecordBtn = midiPianoLayout.getStopRecordBtn();
        stopRecordBtn.setOnClickListener(new onStopClickListener());

    }

    private void setupMidi() {
        // Setup MIDI
        mMidiManager = (MidiManager) getSystemService(MIDI_SERVICE);
        MidiDeviceInfo synthInfo = MidiTools.findDevice(mMidiManager, "AndroidTest",
                "SynthExample");
        int portIndex = 0;
        mPortSelector = new MidiOutputPortConnectionSelector(mMidiManager, this,
                MidiPianoLayout.connectButton , synthInfo, portIndex);
        mPortSelector.setConnectedListener(new MyPortsConnectedListener());
    }

    private void closeSynthResources() {
        if (mPortSelector != null) {
            mPortSelector.close();
        }
    }

    // TODO A better way would be to listen to the synth server
    // for open/close events and then disable/enable the spinner.
    private class MyPortsConnectedListener
            implements MidiPortConnector.OnPortsConnectedListener {
        @Override
        public void onPortsConnected(final MidiDevice.MidiConnection connection) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (connection == null) {
                        Toast.makeText(PersonalModeActivity.this,
                                R.string.error_port_busy, Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(PersonalModeActivity.this,
                                R.string.port_open_ok, Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeSynthResources();
        Debugger.i("######## PersonalActivity Destroy!");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    private static void chageKeyBoard(int KeyNum, int KeyStatus){
        if (KeyStatus == 127){
            MidiPianoLayout.getImageButton(KeyNum).setPressed(true);
        }else if(KeyStatus == 0){
            MidiPianoLayout.getImageButton(KeyNum).setPressed(false);
        }
    }

    private class onStartClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(!isRecording){
                startRecord();
                isRecording = true;
                startRecordBtn.setBackgroundResource(R.drawable.record_pause_selector);
                startTip.setText("正在录音");
                stopTip.setText("停止录音");
                Toast.makeText(PersonalModeActivity.this,"录音开始",Toast.LENGTH_SHORT).show();
            }else{
                isRecording = false;
                //stop();
                startRecordBtn.setBackgroundResource(R.drawable.record_start_selector);
                startTip.setText("录音暂停");
                Toast.makeText(PersonalModeActivity.this,"录音暂停",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startRecord() {
        //parentDirFile = MusicLearnApplication.context.getDir("record_data", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
         Log.d(TAG, "######## startRecord : ");
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss");
        String date = sDateFormat.format(new java.util.Date());
        String filename = date;
        //String filename = "music";
        Log.d(TAG, "######## filename : "+ filename);
        FileOutputStream out = null;
        while(isRecording){
            try {
                out = PersonalModeActivity.this.openFileOutput(filename, Context.MODE_PRIVATE);
                Log.d(TAG, "######## openFileOutput : "+ out.toString());
                out.write(data);
                Log.d(TAG, "######## openFileOutput : "+ out.toString());
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class onStopClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "######## onStopClickListener : ");
            //stopRecord();
            stopRecordBtn.setBackgroundResource(R.drawable.record_stop_selector);
            stopTip.setText("录音完成");
            startTip.setText("开始录音");
            Toast.makeText(PersonalModeActivity.this,"录音完成",Toast.LENGTH_SHORT).show();
        }
    }
    public void readRecord(){
        FileInputStream in = null;
        ByteArrayOutputStream bout = null;
        String filename = "music";
        byte[]buf = new byte[1024];
        bout = new ByteArrayOutputStream();
        int length = 0;
        try {
            in = PersonalModeActivity.this.openFileInput(filename); //获得输入流
            while((length=in.read(buf))!=-1){
                bout.write(buf,0,length);
            }
            byte[] content = bout.toByteArray();
            displayTv.setText(new String(content,"UTF-8")); //设置文本框为读取的内容
        } catch (Exception e) {
            e.printStackTrace();
        }
        displayTv.invalidate(); //刷新屏幕
        try{
            in.close();
            bout.close();
        }
        catch(Exception e){}
    }
}

