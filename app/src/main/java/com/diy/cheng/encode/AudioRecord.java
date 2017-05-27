package com.diy.cheng.encode;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by Administrator on 2017/5/17 0017.
 */

public class AudioRecord {
    android.media.AudioRecord audioRecord;
    private static final int sampleRate = 44100;
    private static final int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    Handler handler;
    private boolean isStart = false;

    public AudioRecord() {
        int minBufSize = android.media.AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
        audioRecord = new android.media.AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, minBufSize);

        HandlerThread handlerThread = new HandlerThread("record_thread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    public void prepare() {
        if (audioRecord == null) {
            return ;
        }
        audioRecord.startRecording();
    }

    public void startRecord() {
        isStart = true;
        handler.post(runnable);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            record();
        }
    };

    public void record() {
        while (isStart) {

        }
    }
}
