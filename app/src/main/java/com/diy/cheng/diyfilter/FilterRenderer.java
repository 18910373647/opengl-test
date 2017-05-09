package com.diy.cheng.diyfilter;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by 0 on 2017/5/9.
 */

public class FilterRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private int surfaceTextureId;
    private SurfaceTexture surfaceTexture;

    private FloatBuffer cubeBuffer;     // 绘制区域buffer
    private FloatBuffer vertexBuffer;  // 纹理buffer
    private Context context;

    private int programeId = -1;
    private int maPositionHandle    = -1;
    private int maTexCoordHandle    = -1;
    private int muPosMtxHandle      = -1;
    private int muTexMtxHandle      = -1;
    private boolean available = false;
    private float[] matrix;
    private float[] matrix1;

    private GLSurfaceView glSurfaceView;

    public FilterRenderer(GLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
        glSurfaceView.setEGLContextClientVersion(2);    // 设置clientVersion
        glSurfaceView.setRenderer(this);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        context = glSurfaceView.getContext();
        matrix = createIdentityMtx();
        matrix1 = createIdentityMtx();

    }

    public static float[] createIdentityMtx() {
        float[] m = new float[16];
        Matrix.setIdentityM(m, 0);
        return m;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // 初始化绘制区域buffer 初始化以后给什么用
        cubeBuffer = ByteBuffer.allocateDirect(OpglUtils.CUBE.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        cubeBuffer.put(OpglUtils.CUBE);
        cubeBuffer.position(0);

        // 初始化纹理buffer 初始化以后给什么用
        vertexBuffer = ByteBuffer.allocateDirect(OpglUtils.TEXTURE_270.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(OpglUtils.TEXTURE_270);
        vertexBuffer.position(0);

        // 创建纹理id 初始化以后给什么用，创建surfaceTexture，给camera做预览
        int[] textureId = new int[1];
        GLES20.glGenTextures(1, textureId, 0);  // 参数 size, 生成的纹理数组，offset
        surfaceTextureId = textureId[0];
        surfaceTexture = new SurfaceTexture(surfaceTextureId);
        surfaceTexture.setOnFrameAvailableListener(this);

        // 启用和禁止gl的一些属性
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_BLEND);

        // 选取纹理单元，并绑定到纹理单元的具体目标（这个目标和具体的片段着色器代码关联）
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, surfaceTextureId);

        // 设置纹理的渲染属性(固定的)
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        //        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        //        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        //        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        //       GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        CameraManager.getInstance(context).openCamera(surfaceTexture);

        // 创建和生成小程序
        programeId = createProgram();

        // 获取小程序中的属性值
        maPositionHandle = GLES20.glGetAttribLocation(programeId, "position");
        maTexCoordHandle = GLES20.glGetAttribLocation(programeId, "inputTextureCoordinate");
        muPosMtxHandle = GLES20.glGetUniformLocation(programeId, "uPosMtx");
        muTexMtxHandle = GLES20.glGetUniformLocation(programeId, "uTexMtx");
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);

        if (available) {
            surfaceTexture.updateTexImage();
            surfaceTexture.getTransformMatrix(matrix);
            available = false;
        }

        GLES20.glUseProgram(programeId);

        cubeBuffer.position(0);
        // 指定要修改的顶点属性的索引值
        // 每个点由几个值表示
        // 指定数组中每个组件的数据类型
        //
        // 指定连续顶点属性之间的偏移量
        //
        GLES20.glVertexAttribPointer(maPositionHandle, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer);
        GLES20.glEnableVertexAttribArray(maPositionHandle);

        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(maTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(maTexCoordHandle);

        if(muPosMtxHandle>= 0) {
            GLES20.glUniformMatrix4fv(muPosMtxHandle, 1, false, matrix1, 0);
        }

        if(muTexMtxHandle>= 0) {
            GLES20.glUniformMatrix4fv(muTexMtxHandle, 1, false, matrix, 0);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(maPositionHandle);
        GLES20.glDisableVertexAttribArray(maTexCoordHandle);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (!available) {
            available = true;
            glSurfaceView.requestRender();
        }
    }

    private int createProgram() {
        int vertexId = OpglUtils.loadShader(GLES20.GL_VERTEX_SHADER, OpglUtils.createVertexShader(context, "gray/vertexshader.glsl"));
        int fragmentId = OpglUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, OpglUtils.createVertexShader(context, "gray/fragmentshader.glsl"));

        int programId = GLES20.glCreateProgram();
        GLES20.glAttachShader(programId, vertexId);
        GLES20.glAttachShader(programId, fragmentId);
        GLES20.glLinkProgram(programId);

        int[] linkId = new int[1];
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkId, 0);
        if (linkId[0] != GLES20.GL_TRUE) {
            Log.e("chengqixiang", "glLinkProgram error");
            GLES20.glDeleteProgram(programId);
            programId = 0;
        }
        return programId;
    }
}
