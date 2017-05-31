package com.diy.cheng.audioencode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;

import com.diy.cheng.videoencode.VideoMuxer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by 0 on 2017/5/27.
 */

public class AudioEncode {
    private static final String MIME = "audio/mp4a-latm";
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL = 2;
    private static final int BIT_RATE = 64000;
    private static final int WAIT_TIME = 12 * 1000;

    MediaCodec mediaCodec;
    MediaCodec.BufferInfo bufferInfo;

    private static final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "audioEncode.aac";
    private File file;
    private FileOutputStream fos;

    VideoMuxer videoMuxer;

    public AudioEncode() {
        MediaFormat format = MediaFormat.createAudioFormat(MIME, SAMPLE_RATE, CHANNEL);
        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);//比特率
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
//        format.setByteBuffer("csd-0", );
        try {
            mediaCodec = MediaCodec.createEncoderByType(MIME);
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            file.setReadable(true);
            file.setWritable(true);
            file.setExecutable(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        videoMuxer = new VideoMuxer(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "audio.mp4");
        bufferInfo = new MediaCodec.BufferInfo();
    }

    public void prepare() {
        if (mediaCodec == null) {
            return ;
        }
        mediaCodec.start();
    }

    public void start(byte[] data, int len, long time) {
        if (mediaCodec == null) {
            return ;
        }

        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        int inputIndex = mediaCodec.dequeueInputBuffer(WAIT_TIME);
        if (inputIndex >= 0) {
            ByteBuffer byteBuffer = inputBuffers[inputIndex];
            byteBuffer.clear();
            byteBuffer.put(data);
            mediaCodec.queueInputBuffer(inputIndex, 0, len, time, 0);
        }

        ByteBuffer[] outBuffers = mediaCodec.getOutputBuffers();
        int outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, WAIT_TIME);
        if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            Log.e("chengqixiang", "AudioEncode outformat changed");
            MediaFormat format = mediaCodec.getOutputFormat();
            videoMuxer.prepare(format);
        } else if (outIndex >= 0) {
            ByteBuffer byteBuffer = outBuffers[outIndex];
            int length = bufferInfo.size + 7;
            byte[] encodeData = new byte[length];
            addADTStoPacket(encodeData, length);
            byteBuffer.get(encodeData, 7, bufferInfo.size);
            try {
                fos.write(encodeData, bufferInfo.offset, length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaCodec.releaseOutputBuffer(outIndex, false);
            // TODO 需要放到releaseOutputBuffer后边，否则会crash
            if (bufferInfo.flags != MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                videoMuxer.start(byteBuffer, bufferInfo);
            }
        }
    }

    /**
     * ADTS 头中相对有用的信息 采样率、声道数、帧长度
     * 给编码出的aac裸流添加adts头字段
     * @param packet 要空出前7个字节，否则会搞乱数据
     * @param packetLen
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2;  //AAC LC
        int freqIdx = 4;  //44.1KHz
        int chanCfg = 2;  //CPE
        packet[0] = (byte)0xFF;
        packet[1] = (byte)0xF9;
        packet[2] = (byte)(((profile-1)<<6) + (freqIdx<<2) +(chanCfg>>2));
        packet[3] = (byte)(((chanCfg&3)<<6) + (packetLen>>11));
        packet[4] = (byte)((packetLen&0x7FF) >> 3);
        packet[5] = (byte)(((packetLen&7)<<5) + 0x1F);
        packet[6] = (byte)0xFC;
    }

    public void stop() {
        if (mediaCodec != null) {
            mediaCodec.flush();
            mediaCodec.stop();
            videoMuxer.stop();
        }
    }

    public void release() {
        if (mediaCodec != null) {
            mediaCodec.release();
            mediaCodec = null;
        }

        if (fos != null) {
            try {
                fos.flush();
                fos.close();
                fos = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
