package com.diy.cheng.encode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;

import com.diy.cheng.opengl.InputSurface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by 0 on 2017/5/9.
 */

public class CameraRecord {
    MediaCodec mediaCodec;
    MediaCodec.BufferInfo bufferInfo;
    InputSurface inputSurface;
    Handler handler;
    HandlerThread handlerThread;

    boolean isStart = false;
    private ReentrantLock reentrantLock = new ReentrantLock();  // TODO 看看java锁
    private VideoMuxer videoMuxer;

    public CameraRecord(int width, int height) {
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 1300 * 1024);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
        format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
        format.setInteger(MediaFormat.KEY_COMPLEXITY, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);

        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        videoMuxer = new VideoMuxer();

        bufferInfo = new MediaCodec.BufferInfo();
        // TODO HandlerThread 具体用来做什么
        handlerThread = new HandlerThread("mediacodec_encode");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    public void makeCurrent() {
        if (inputSurface != null) {
            inputSurface.makeCurrent();
        }
    }

    public void swapBuffers() {
        if (inputSurface != null) {
            inputSurface.swapBuffers();
            inputSurface.setPresentationTime(System.nanoTime());
        }
    }

    public boolean prepareRecord() {
        if (mediaCodec == null || inputSurface != null) {
            return false;
        }

        try {
            inputSurface = new InputSurface(mediaCodec.createInputSurface());
            mediaCodec.start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return true;
    }

    public void startRecord() {
        isStart = true;
        handler.post(runnable);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            drainEncode();
        }
    };

    public void stopRecord() {
        if (!isStart) {
            return ;
        }
        isStart = false;
        handler.removeCallbacks(null);
        handlerThread.quit();
        reentrantLock.lock();
        releaseEncode();
        reentrantLock.unlock();
    }

    public void drainEncode() {
        ByteBuffer[] outputBuffer = mediaCodec.getOutputBuffers();
        while (isStart) {
            reentrantLock.lock();
            if (mediaCodec != null) {
                int outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 12 * 1000);
                if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat mediaFormat = mediaCodec.getOutputFormat();
                    videoMuxer.prepare(mediaFormat);
                } else if (outputIndex >= 0) {
                    ByteBuffer byteBuffer = outputBuffer[outputIndex];
                    if (videoMuxer != null && bufferInfo.flags != MediaCodec.BUFFER_FLAG_CODEC_CONFIG && bufferInfo.size > 11) { // BUFFER_FLAG_CODEC_CONFIG编码器信息，而不是媒体数据
                        videoMuxer.start(byteBuffer, bufferInfo);
                    }
                    mediaCodec.releaseOutputBuffer(outputIndex, false);
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                reentrantLock.unlock();
            } else {
                reentrantLock.unlock();
                break;
            }
        }
    }

    private void releaseEncode() {
        if (videoMuxer != null) {
            videoMuxer.stop();
            videoMuxer = null;
        }

        if (mediaCodec != null) {
            mediaCodec.signalEndOfInputStream();    //   因为Surface作为input的时候不会主动向Codec传递结束标志，
                                                    //   所以得手动调用m_MediaCodec.signalEndOfInputStream();
                                                    //   这时Surface将停止向Codec传输数据。注意此函数只有当Surface作为input时才能调用。
                                                    //   结束编码记得release和stop
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }

        if (inputSurface != null) {
            inputSurface.release();
            inputSurface = null;
        }
    }
}
