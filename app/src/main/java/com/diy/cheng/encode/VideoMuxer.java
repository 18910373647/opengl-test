package com.diy.cheng.encode;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by 0 on 2017/5/10.
 */

public class VideoMuxer {
    private MediaMuxer mediaMuxer;
    private static final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "camera.mp4";
    private int trackIndex = -1;
    private boolean isAddTrack = false;
    private boolean isStartMuxer = false;
    private ReentrantLock reentrantLock = new ReentrantLock();

    public VideoMuxer() {
        try {
            mediaMuxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void prepare(MediaFormat mediaFormat) {
        trackIndex = mediaMuxer.addTrack(mediaFormat);
        Log.e("chengqixiang", "trackIndex ==== " + trackIndex);
        mediaMuxer.start();
        isAddTrack = true;
    }

    public void start(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
        if (mediaMuxer == null || trackIndex == -1) {
            return ;
        }

        if (isAddTrack) {
            reentrantLock.lock();
            isStartMuxer = true;
            Log.e("chengqixiang", "bufferInfo.size ==== " + bufferInfo.size + "bufferInfo.offset === " + bufferInfo.offset);
            mediaMuxer.writeSampleData(trackIndex, byteBuffer, bufferInfo);
            reentrantLock.unlock();
        }
    }

    public void stop() {
        if (!isAddTrack) {
            return ;
        }

        reentrantLock.lock();
        release();
        reentrantLock.unlock();
    }

    public void release() {
        if (mediaMuxer != null) {
            if (isStartMuxer) {
                mediaMuxer.stop();
                mediaMuxer.release();

                isStartMuxer = false;
            }
        }
        isAddTrack = false;
    }
}
