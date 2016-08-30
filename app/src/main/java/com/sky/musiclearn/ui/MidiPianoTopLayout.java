package com.sky.musiclearn.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.sky.musiclearn.PersonalModeActivity;
import com.sky.musiclearn.R;
import com.sky.musiclearn.audiorecord.AudioFileUtils;
import com.sky.musiclearn.audiorecord.AudioRecordManager;
import com.sky.musiclearn.audiorecord.ErrorCode;
import com.sky.musiclearn.personalmodel.VisualizerLeftView;
import com.sky.musiclearn.personalmodel.VisualizerRightView;
import com.sky.musiclearn.utils.MusicLearnApplication;
import com.sky.musiclearn.utils.MusicLearnConfig;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Sky000 on 2016/8/12.
 */
public class MidiPianoTopLayout extends FrameLayout {
    Context context;
    AudioRecordManager audioRecordManager;
    ViewFlipper musicBookFlipper;
    FrameLayout musicBookView;
    Button startRecordBtn;
    Button stopRecordBtn;
    ImageView musicTitle;
    ImageView musicPage1,musicPage2,musicPage3;
    VisualizerLeftView visualizerLeftView;
    VisualizerRightView visualizerRightView;
    private static final float VISUALIZER_HEIGHT_DIP = 150f;//频谱View高度
    private boolean flag = true;
    TextView startTip;
    TextView stopTip;
    TextView displayTv;
    private final static int FLAG_WAV = 0;
    private final static int FLAG_AMR = 1;
    private int mState = -1;    //-1:没再录制，0：录制wav，1：录制amr
    private final static int CMD_RECORDING_TIME = 2000;
    private final static int CMD_RECORDFAIL = 2001;
    private final static int CMD_STOP = 2002;

    private int[] imgs = { R.drawable.music_book_01, R.drawable.music_book_02,
            R.drawable.music_book_03, R.drawable.music_book_04 };

    public MidiPianoTopLayout(Context context) {
        super(context);
        this.context = context;
        setBackgroundColor(Color.TRANSPARENT);
        audioRecordManager = AudioRecordManager.getInstance();

        visualizerLeftView = new VisualizerLeftView(context);
        FrameLayout.LayoutParams visualizerLeftParams = new FrameLayout.LayoutParams(
                MusicLearnConfig.getResolutionValue(600),
                MusicLearnConfig.getResolutionValue(400));
        visualizerLeftParams.topMargin = MusicLearnConfig.getResolutionValue(40);
        visualizerLeftParams.leftMargin = MusicLearnConfig.getResolutionValue(30);
        visualizerLeftView.setLayoutParams(visualizerLeftParams);
        addView(visualizerLeftView);

        visualizerRightView = new VisualizerRightView(context);
        FrameLayout.LayoutParams visualizerRightParams = new FrameLayout.LayoutParams(
                MusicLearnConfig.getResolutionValue(800),
                MusicLearnConfig.getResolutionValue(400));
        visualizerRightParams.topMargin = MusicLearnConfig.getResolutionValue(60);
        visualizerRightParams.leftMargin = MusicLearnConfig.getResolutionValue(1920-450);
        visualizerRightView.setLayoutParams(visualizerRightParams);
        addView(visualizerRightView);

        musicTitle = new ImageView(context);
        LayoutParams musicTitlewParams = new LayoutParams(MusicLearnConfig.getResolutionValue(500),
                MusicLearnConfig.getResolutionValue(60));
        musicTitlewParams.topMargin = MusicLearnConfig.getResolutionValue(50);
        musicTitlewParams.leftMargin = MusicLearnConfig.getResolutionValue(650);
        musicTitle.setLayoutParams(musicTitlewParams);
        musicTitle.setImageResource(R.drawable.music_book_title);
        addView(musicTitle);

        musicBookView = new FrameLayout(context);
        LayoutParams musicBookViewParams = new LayoutParams(MusicLearnConfig.getResolutionValue(1272),
                MusicLearnConfig.getResolutionValue(433));
        musicBookViewParams.topMargin = MusicLearnConfig.getResolutionValue(50);
        musicBookViewParams.leftMargin = MusicLearnConfig.getResolutionValue(250);
        musicBookView.setLayoutParams(musicBookViewParams);
        musicBookView.setFocusable(true);
        musicBookView.requestFocus();
        musicBookView.setOnClickListener(new  onMusicBookClickListener());
        musicBookView.setBackgroundResource(R.drawable.music_book_selector);
        addView(musicBookView);

        musicBookFlipper = new ViewFlipper(context);
        LayoutParams musicFlipperParams = new LayoutParams(MusicLearnConfig.getResolutionValue(1024),
                MusicLearnConfig.getResolutionValue(280));
        musicFlipperParams.topMargin = MusicLearnConfig.getResolutionValue(88);
        musicFlipperParams.leftMargin = MusicLearnConfig.getResolutionValue(121);
        musicBookFlipper.setLayoutParams(musicFlipperParams);
        addViewToFlipper();
        musicBookView.addView(musicBookFlipper);


        startRecordBtn = new Button(context);
        LayoutParams startParams = new LayoutParams(MusicLearnConfig.getResolutionValue(100),
                MusicLearnConfig.getResolutionValue(100));
        startParams.topMargin = MusicLearnConfig.getResolutionValue(150);
        startParams.leftMargin = MusicLearnConfig.getResolutionValue(100);
        startRecordBtn.setLayoutParams(startParams);
        startRecordBtn.setFocusable(true);
        startRecordBtn.setBackgroundResource(R.drawable.record_start_selector);
        //startRecordBtn.setOnClickListener(new onStartClickListener());
        addView(startRecordBtn);

        startTip = new TextView(context);
        LayoutParams startTipParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        startTipParams.topMargin = MusicLearnConfig.getResolutionValue(190);
        startTipParams.leftMargin = MusicLearnConfig.getResolutionValue(220);
        startTip.setLayoutParams(startTipParams);
        startTip.setText("开始录音");
        addView(startTip);

        stopRecordBtn = new Button(context);
        LayoutParams stopParams = new LayoutParams(MusicLearnConfig.getResolutionValue(100),
                MusicLearnConfig.getResolutionValue(100));
        stopParams.topMargin = MusicLearnConfig.getResolutionValue(300);
        stopParams.leftMargin = MusicLearnConfig.getResolutionValue(100);
        stopRecordBtn.setLayoutParams(stopParams);
        stopRecordBtn.setFocusable(true);
        stopRecordBtn.setBackgroundResource(R.drawable.record_stop_selector);
        //stopRecordBtn.setOnClickListener(new onStopClickListener());
        addView(stopRecordBtn);

        stopTip = new TextView(context);
        LayoutParams stopTipParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        stopTipParams.topMargin = MusicLearnConfig.getResolutionValue(340);
        stopTipParams.leftMargin = MusicLearnConfig.getResolutionValue(220);
        stopTip.setLayoutParams(stopTipParams);
        stopTip.setText("停止录音");
        addView(stopTip);

        displayTv = new TextView(context);
        LayoutParams displayParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        displayParams.topMargin = MusicLearnConfig.getResolutionValue(400);
        displayParams.leftMargin = MusicLearnConfig.getResolutionValue(100);
        displayTv.setLayoutParams(displayParams);
        addView(displayTv);

    }
    public Button getStartRecordBtn(){ return startRecordBtn;}
    public Button getStopRecordBtn(){ return stopRecordBtn; }
    public TextView getStartTip(){ return startTip;}
    public TextView getStopTip(){ return  stopTip;}
    public TextView getDisplayTv(){return displayTv;}

    public VisualizerLeftView getVisualizerLeftView(){
        return visualizerLeftView;
    }
    public VisualizerRightView getVisualizerRightView(){
        return visualizerRightView;
    }

    public void addViewToFlipper(){
        for (int i = 0; i < imgs.length; i++) {          // 添加图片源
            ImageView musicPage = new ImageView(context);
            LayoutParams musicPageParams = new LayoutParams(MusicLearnConfig.getResolutionValue(1024),
                    MusicLearnConfig.getResolutionValue(280));
            musicPage.setLayoutParams(musicPageParams);
            //musicPage.setScaleType(ImageView.ScaleType.FIT_CENTER);//将图片等比例缩放,居中显示
            musicPage.setImageResource(imgs[i]);
            musicPage.setScaleType(ImageView.ScaleType.FIT_XY);
            musicBookFlipper.addView(musicPage);
        }
    }

 public class onMusicBookClickListener implements View.OnClickListener{
     @Override
     public void onClick(View v) {
         if(flag == true){
             musicBookFlipper.setAutoStart(true);
             musicBookFlipper.setFlipInterval(12000);    //时间间隔
             slideLeft(musicBookFlipper,context);
             musicBookFlipper.startFlipping(); //点击事件后，自动播放
             flag = false;
             Toast.makeText(context,"练习开始",Toast.LENGTH_SHORT).show();
         } else if(flag == false){
             musicBookFlipper.setAutoStart(false);
             musicBookFlipper.stopFlipping(); // 再次点击又就停止播放
             flag = true;
             Toast.makeText(context,"练习暂停",Toast.LENGTH_SHORT).show();
         }
     }
 }
    public void slideLeft(ViewFlipper v,Context c){
        v.animate().cancel();
        Animation inAnim = AnimationUtils.loadAnimation(c,R.anim.anim_right_in);// 添加自定义in，out动画
        inAnim.setDuration(1000);
        Animation outAnim = AnimationUtils.loadAnimation(c,R.anim.anim_left_out);
        outAnim.setDuration(1000);
        v.setInAnimation(inAnim);
        v.setOutAnimation(outAnim);
    }

}
