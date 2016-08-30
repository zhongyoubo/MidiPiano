/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sky.musiclearn.synth.play;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.util.Log;

import com.sky.musiclearn.personalmodel.VisualizerLeftView;
import com.sky.musiclearn.personalmodel.VisualizerRightView;
import com.sky.musiclearn.studymodel.VisualizerView;
import com.sky.musiclearn.ui.MidiPianoLayout;

/**
 * Simple base class for implementing audio output for examples.
 * This can be sub-classed for experimentation or to redirect audio output.
 */
public class SimpleAudioOutput {

    private static final String TAG = "AudioOutputTrack";
    public static final int SAMPLES_PER_FRAME = 2;
    public static final int BYTES_PER_SAMPLE = 4; // float
    public static final int BYTES_PER_FRAME = SAMPLES_PER_FRAME * BYTES_PER_SAMPLE;
    private AudioTrack mAudioTrack;
    private int mFrameRate;
    AudioTrack player;
    int audioSessionId;
    private MediaPlayer mMediaPlayer;//音频
    private Visualizer mVisualizerLeft,mVisualizerRight;//频谱器
    private Equalizer mEqualizer; //均衡器
    VisualizerLeftView visualizerLeftView;
    VisualizerRightView visualizerRightView;

    /**
     *
     */
    public SimpleAudioOutput() {
        super();
    }

    /**
     * Create an audio track then call play().
     *
     * @param frameRate
     */
    public void start(int frameRate) {
        stop();
        mFrameRate = frameRate;
        mAudioTrack = createAudioTrack(frameRate);
        // AudioTrack will wait until it has enough data before starting.
        mAudioTrack.play();

        if(mAudioTrack !=null){
            //实例化Visualizer，参数SessionId可以通过MediaPlayer的对象获得;
            int audioSessionId = mAudioTrack.getAudioSessionId();
            mVisualizerRight = new Visualizer(audioSessionId);
            Log.i(TAG,"###### mAudioTrack.getAudioSessionId(): "+ audioSessionId);
        }else{
            Log.i(TAG,"###### mAudioTrack = null ");
        }
        visualizerRightView = MidiPianoLayout.getVisualizerRightView();
        if(mVisualizerRight != null){
            mVisualizerRight.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            //设置允许波形表示，并且捕获它
            visualizerRightView.setVisualizer(mVisualizerRight);
            mVisualizerRight.setEnabled(true);//false 则不显示
        }else{  Log.i(TAG,"###### mVisualizerRight = null ");}
    }

    public AudioTrack createAudioTrack(int frameRate) {
        int minBufferSizeBytes = AudioTrack.getMinBufferSize(frameRate,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_FLOAT);
        Log.i(TAG, "AudioTrack.minBufferSize = " + minBufferSizeBytes
                + " bytes = " + (minBufferSizeBytes / BYTES_PER_FRAME)
                + " frames");
        int bufferSize = 8 * minBufferSizeBytes / 8;
        int outputBufferSizeFrames = bufferSize / BYTES_PER_FRAME;
        Log.i(TAG, "actual bufferSize = " + bufferSize + " bytes = "
                + outputBufferSizeFrames + " frames");

        player = new AudioTrack(AudioManager.STREAM_MUSIC,
                mFrameRate, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_FLOAT, bufferSize,
                AudioTrack.MODE_STREAM);
        Log.i(TAG, "created AudioTrack");
        return player;
    }


    public int write(float[] buffer, int offset, int length) {
        return mAudioTrack.write(buffer, offset, length,
                AudioTrack.WRITE_BLOCKING);
    }

    public void stop() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack = null;
        }
    }

    public int getFrameRate() {
        return mFrameRate;
    }

    public AudioTrack getAudioTrack() {
        return mAudioTrack;
    }
    public int getAudioTrackId() {
        return audioSessionId;
    }
}
