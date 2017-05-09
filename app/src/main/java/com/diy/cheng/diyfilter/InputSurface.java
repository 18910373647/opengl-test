package com.diy.cheng.diyfilter;

import android.opengl.EGL14;
import android.opengl.EGLDisplay;
import android.util.Log;
import android.view.Surface;

/**
 * Created by 0 on 2017/5/9.
 */

public class InputSurface {
    Surface surface;
    EGLDisplay eglDisplay;

    public InputSurface(Surface surface) {
        this.surface = surface;
        initEgl();
    }

    private void initEgl() {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.e("chengqixiang", "eglGetDisplay error");
            return ;
        }
    }
}
