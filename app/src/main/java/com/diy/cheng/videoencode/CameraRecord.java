package com.diy.cheng.videoencode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by 0 on 2017/5/9.
 */

public class CameraRecord {
    MediaCodec mediaCodec;
    MediaCodec.BufferInfo bufferInfo;
    Handler handler;
    HandlerThread handlerThread;

    boolean isStart = false;
    private ReentrantLock reentrantLock = new ReentrantLock();  // TODO 看看java锁
    private VideoMuxer videoMuxer;
    private static final int WAIT_TIME = 12 * 1000;

    private int width;
    private int height;

    public CameraRecord(int width, int height) {
        this.width = width;
        this.height = height;

        Log.e("chengqixiang", "width === " + width + " height === " + height);
        // TODO 1，因为preview的图像是横向的，需要将宽高进行翻转下。
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", height, width);
        // TODO 2，camera支持的预览NV21 -- YYYY YYYY VUVU,mediaCodec支持的格式YUV420SP(NV12) -- YYYY YYYY UVUV
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 1300 * 1024);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        videoMuxer = new VideoMuxer();
        bufferInfo = new MediaCodec.BufferInfo();

        handlerThread = new HandlerThread("mediacodec_encode");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    public boolean prepareRecord() {
        if (mediaCodec == null) {
            return false;
        }
        mediaCodec.start();
        return true;
    }

    public void startRecord() {
        isStart = true;
    }

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

    public void drainCameraEncode(byte[] src) {
        if (mediaCodec == null) {
            return ;
        }

        if (!isStart) {
            return ;
        }
        byte[] data1;
        byte[] data;

        // 处理下数据，预览方向正确，但是取出的数据方向不正确，UV数据不正确
        // TODO 3,预览previewSize大小也有关
        data1 = changeUV(src);
        data = rotate(data1);
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        int inputIndex = mediaCodec.dequeueInputBuffer(WAIT_TIME);

        if (inputIndex >= 0) {
            ByteBuffer byteBuffer = inputBuffers[inputIndex];
            byteBuffer.clear();
            byteBuffer.put(data);
            mediaCodec.queueInputBuffer(inputIndex, 0, data.length, 0, 0);
        }

        ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
        int outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, WAIT_TIME);

        if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            // TODO 4，初始化MediaMuxer
            MediaFormat format = mediaCodec.getOutputFormat();
            videoMuxer.prepare(format);
        } else if (outputIndex >= 0) {
            ByteBuffer byteBuffer = outputBuffers[outputIndex];
            if (bufferInfo.flags != MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                videoMuxer.start(byteBuffer, bufferInfo);
            }
            mediaCodec.releaseOutputBuffer(outputIndex, false);
        }
    }

    public byte[] rotate(byte[] data) {
        byte[] tmp = new byte[width * height * 3 / 2];
        int wh = width * height;
        int k = 0;

        // 旋转Y
        for (int i = width - 1; i >= 0; i--) {
            for (int j = height - 1; j >= 0; j--) {
                tmp[k] = data[width * j + i];
                k++;
            }
        }

        // 旋转UV
        for (int i = width - 2; i >= 0; i -= 2) {
            for (int j = height / 2 - 1; j >= 0; j--) {
                tmp[k] = data[wh + width * j + i];
                tmp[k + 1] = data[wh + width * j + i + 1];
                k += 2;
            }
        }
        return tmp;
    }

    // 摄像头支持的视频输出格式NV21 -- YYYY YYYY VU VU
    // mediaCodec支持的NV12 -- YYYY YYYY UV UV
    // 需要将VU --> VU
    public byte[] changeUV(byte[] data) {
        byte[] tmp = new byte[width * height * 3 / 2];
        for (int i = 0; i < width * height * 3 / 2; i++) {
            if (i < width * height) {
                tmp[i] = data[i];
            } else if (i % 2 == 0) {
                tmp[i + 1] = data[i];
            } else {
                tmp[i - 1] = data[i];
            }
        }
        return tmp;
    }

    private void releaseEncode() {
        if (videoMuxer != null) {
            videoMuxer.stop();
            videoMuxer = null;
        }

        if (mediaCodec != null) {
//            if (encodeType == EncodeType.SURFACEENCODE) {
//                mediaCodec.signalEndOfInputStream();    //   因为Surface作为input的时候不会主动向Codec传递结束标志，
                                                        //   所以得手动调用m_MediaCodec.signalEndOfInputStream();
                                                        //   这时Surface将停止向Codec传输数据。注意此函数只有当Surface作为input时才能调用//   结束编码记得release和stop
//            }
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }
    }
}
