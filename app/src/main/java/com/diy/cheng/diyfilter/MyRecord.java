package com.diy.cheng.diyfilter;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.io.IOException;

/**
 * Created by 0 on 2017/5/9.
 */

public class MyRecord {
    MediaCodec mediaCodec;
    MediaCodec.BufferInfo bufferInfo;

    public MyRecord(int width, int height) {
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 256000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

        try {
            mediaCodec = MediaCodec.createEncoderByType(format.getString("video/avc"));
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        bufferInfo = new MediaCodec.BufferInfo();
    }

    public void startRecord() {
        mediaCodec.createInputSurface();
        mediaCodec.start();
    }
}
