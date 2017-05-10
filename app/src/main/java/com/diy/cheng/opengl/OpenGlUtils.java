package com.diy.cheng.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 0 on 2017/5/9.
 */

public class OpenGlUtils {
    public static final float[] CUBE = new float[] {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f
    };

    public static final float[] TEXTURE = new float[] {
//            0.0f, 0.0f,
//            0.0f, 1.0f,
//            1.0f, 0.0f,
//            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    public static final float[] TEXTURE_90 = new float[] {
//            1.0f, 0.0f,
//            0.0f, 0.0f,
//            1.0f, 1.0f,
//            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
    };

    public static final float[] TEXTURE_180 = new float[] {
//            1.0f, 1.0f,
//            1.0f, 0.0f,
//            0.0f, 1.0f,
//            0.0f, 0.0f,

            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
    };

    public static final float[] TEXTURE_270 = new float[] {
//            0.0f, 1.0f,   // 需要横向翻转
//            1.0f, 1.0f,
//            0.0f, 0.0f,
//            1.0f, 0.0f,

            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };

    public static String createVertexShader(Context context, String fileName) {
        String fileContent = "";
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            StringBuffer out = new StringBuffer();
            byte[] b = new byte[4096];
            int len;
            while ((len = inputStream.read(b)) != -1) {
                out.append(new String(b, 0, len));
            }

            fileContent = out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileContent;
    }

    public static int loadShader(int shaderType, String source) {
        int shaderId = GLES20.glCreateShader(shaderType);
        GLES20.glShaderSource(shaderId, source);
        GLES20.glCompileShader(shaderId);

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("chengqixiang", "glCreateShader error");
            GLES20.glDeleteShader(shaderId);
            shaderId = 0;
        }
        return shaderId;
    }
}
