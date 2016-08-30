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

package com.sky.musiclearn.service;

import android.content.Intent;
import android.media.AudioTrack;
import android.media.midi.MidiDeviceService;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiReceiver;
import android.os.Binder;
import android.os.IBinder;

import com.sky.musiclearn.synth.play.SynthEngine;


public class MidiSynthDeviceService extends MidiDeviceService {


    private SynthEngine mSynthEngine = new SynthEngine();
    private boolean mSynthStarted = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mSynthEngine.stop();
        super.onDestroy();
    }

    @Override
    public MidiReceiver[] onGetInputPortReceivers() {
        return new MidiReceiver[]{mSynthEngine};
    }

    /**
     * This will get called when clients connect or disconnect.
     */
    @Override
    public void onDeviceStatusChanged(MidiDeviceStatus status) {
        if (status.isInputPortOpen(0) && !mSynthStarted) {
            mSynthEngine.start();
            mSynthStarted = true;
        } else if (!status.isInputPortOpen(0) && mSynthStarted) {
            mSynthEngine.stop();
            mSynthStarted = false;
        }
    }

    public  AudioTrack getAudioTrack(){
        return  mSynthEngine.getAudioTrack();
    }
   /* *//**
     * 返回一个Binder对象
     *//*
    @Override
    public IBinder onBind(Intent intent) {
        return new MidiSynthServiceBinder();
    }

    public class MidiSynthServiceBinder extends Binder {
        *//**
         * 获取当前Service的实例
         * @return
         *//*
        public MidiSynthDeviceService getService(){
            return MidiSynthDeviceService.this;
        }
    }*/

}
