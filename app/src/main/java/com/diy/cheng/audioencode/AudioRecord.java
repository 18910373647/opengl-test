package com.diy.cheng.audioencode;

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
    byte[] buffer;

    AudioEncode audioEncode;

    public AudioRecord() {
        int minBufSize = android.media.AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
        audioRecord = new android.media.AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, minBufSize);
        buffer = new byte[minBufSize / 2];

        audioEncode = new AudioEncode();
        audioEncode.prepare();

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

    public void stopRecord() {
        isStart = false;
        audioRecord.stop();
        audioEncode.stop();
    }

    public void releaseRecord() {
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }

        if (audioEncode != null) {
            audioEncode.release();
            audioEncode = null;
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            record();
        }
    };
    long time = 0;

    public void record() {
        while (isStart) {
            int len = audioRecord.read(buffer, 0, buffer.length);
            time = System.nanoTime() / 1000;
            audioEncode.start(buffer, len, time);
        }
    }
}
