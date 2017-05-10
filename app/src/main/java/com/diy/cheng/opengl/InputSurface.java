package com.diy.cheng.opengl;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.util.Log;
import android.view.Surface;

/**
 * Created by 0 on 2017/5/9.
 */

public class InputSurface {
    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    Surface surface;
    EGLDisplay eglDisplay = null;
    EGLContext eglContext = null;
    EGLSurface eglSurface = null;

    public InputSurface(Surface surface) {
        this.surface = surface;
        initEgl();  // 启动EGL
    }

    public void makeCurrent() {
        boolean maked = EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
        if (!maked) {
            Log.e("chengqixiang", "eglMakeCurrent error");
        }
    }

    public void swapBuffers() {
        boolean swaped = EGL14.eglSwapBuffers(eglDisplay, eglSurface);
        if (!swaped) {
            Log.e("chengqixiang", "eglSwapBuffers error");
        }
    }

    public void setPresentationTime(long nsecs) {
        EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface, nsecs);
    }

    private void initEgl() {
        // 1,获取EGLDiaplay
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.e("chengqixiang", "eglGetDisplay error");
            return ;
        }

        // 2,初始化EGLDiaplay，并获取egl版本号
        int[] versions = new int[2];
        boolean inited = EGL14.eglInitialize(eglDisplay, versions, 0, versions, 1);
        if (!inited) {
            Log.e("chengqixiang", "eglInitialize error");
            return ;
        }

        // 3,设置EGL的配置参数
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE,
                EGL14.EGL_OPENGL_ES2_BIT,
                EGL_RECORDABLE_ANDROID,
                1,
                EGL14.EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        // 2 配置参数，3配置参数offset，4，需要获取的配置参数，5，configs offset，6,configs length, 7,系统总的配置参数，8，系统总的配置参数offset
        boolean choosed = EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, configs.length, numConfigs, 0);
        if (!choosed) {
            Log.e("chengqixiang", "eglChooseConfig error");
            return ;
        }

        // 创建EGLContext
        int[] attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION,
                2,
                EGL14.EGL_NONE
        };

        eglContext = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.eglGetCurrentContext(), attrib_list, 0);
        checkEglError();
        if (eglContext == null) {
            Log.e("chengqixiang", "eglCreateContext error");
            return ;
        }

        // 创建EGLSurface
        int[] surfaceAttrs = {
            EGL14.EGL_NONE
        };
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, configs[0], surface, surfaceAttrs, 0);
        checkEglError();
        if (eglSurface == null) {
            Log.e("chengqixiang", "eglCreateWindowSurface error");
            return ;
        }
    }

    public void checkEglError() {
        int error;
        if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
            Log.e("chengqixiang", "error === 0x" + Integer.toHexString(error)); // Integer.toHexString(error) 转换为16进制
            throw new RuntimeException("checkEglError error === 0x" + Integer.toHexString(error));
        }
    }

    public void release() {
        EGL14.eglDestroySurface(eglDisplay, eglSurface);
        EGL14.eglDestroyContext(eglDisplay, eglContext);
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(eglDisplay);

        surface.release();

        eglDisplay = null;
        eglContext = null;
        eglSurface = null;
        surface = null;
    }
}
