package com.sky.musiclearn;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.sky.musiclearn.service.MidiSynthDeviceService;
import com.sky.musiclearn.studymodel.StudyModelView;
import com.sky.musiclearn.studymodel.VisualizerView;
import com.sky.musiclearn.ui.MidiPianoLayout;
import com.sky.musiclearn.utils.Debugger;

/**
 * Created by Sky000 on 2016/8/17.
 * 学习模式
 */
public class StudyModelActivity extends Activity{
    StudyModelView studyModelView;
    private static final float VISUALIZER_HEIGHT_DIP = 150f;//频谱View高度
    private MediaPlayer mMediaPlayer;//音频
    private Visualizer mVisualizer;//频谱器
    private Equalizer mEqualizer; //均衡器
    VisualizerView visualizerView;
    MidiSynthDeviceService midiSynthDeviceService;
    AudioTrack audioTrack;
    private String TAG = "sta";
    ImageButton play;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Debugger.i("########## StudyModelActivity is created");
        setVolumeControlStream(AudioManager.STREAM_MUSIC);//设置音频流 - STREAM_MUSIC：音乐回放即媒体音量
        studyModelView = new StudyModelView(this);
        setContentView(studyModelView);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);
        }
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏

        //绑定Service
       // Intent intent = new Intent(StudyModelActivity.this,MidiSynthDeviceService.class);
        //bindService(intent, conn, Context.BIND_AUTO_CREATE);

    /*    mMediaPlayer = new MediaPlayer();
        mMediaPlayer = MediaPlayer.create(this, R.raw.hd);//实例化 MediaPlayer 并添加音频
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //mMediaPlayer.start();//开始播放
        mMediaPlayer.setLooping(true);//循环播放

        mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());
        mEqualizer.setEnabled(true);// 启用均衡器
        setupVisualizerFxAndUi();//添加频谱到界面
        mVisualizer.setEnabled(true);//false 则不显示

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mVisualizer.setEnabled(false);
            }
        });*/
    }


    /**
     * 生成一个VisualizerView对象，使音频频谱的波段能够反映到 VisualizerView上
     */
    private void setupVisualizerFxAndUi() {
        visualizerView = studyModelView.getVisualizerView();

        Log.i(TAG,"###### setupVisualizerFxAndUi ");
        //实例化Visualizer，参数SessionId可以通过MediaPlayer的对象获得
        Log.i(TAG,"###### mMediaPlayer.getAudioSessionId(): "+ mMediaPlayer.getAudioSessionId());
        Log.i(TAG,"###### Visualizer.getCaptureSizeRange()[1]: "+ Visualizer.getCaptureSizeRange()[1]);
        mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());

        Log.i(TAG,"###### mMediaPlayer.getAudioSessionId(): "+ mMediaPlayer.getAudioSessionId());
        //采样 - 参数内必须是2的位数 - 如64,128,256,512,1024
        if(mVisualizer !=null){
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            Log.i(TAG,"###### Visualizer.getCaptureSizeRange()[1]: "+ Visualizer.getCaptureSizeRange()[1]);
            //设置允许波形表示，并且捕获它
            visualizerView.setVisualizer(mVisualizer);
            Log.i(TAG,"###### mVisualizer: "+ mVisualizer.toString());
        }else{
            Log.i(TAG,"###### mVisualizer = null ");
        }

    }
    @Override
    protected void onDestroy() {
        mMediaPlayer.stop();
        super.onDestroy();
    }
   /* ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //返回一个MsgService对象
            midiSynthDeviceService = ((MidiSynthDeviceService.MidiSynthServiceBinder)service).getService();
            audioTrack = midiSynthDeviceService.getAudioTrack();
        }
    };
*/


}
