package com.diy.cheng.diyfilter;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.diy.cheng.camera.CameraEngine;
import com.diy.cheng.encode.CameraRecord;

/**
 * Created by 0 on 2017/5/10.
 */

public class CameraActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener, Camera.PreviewCallback {
    SurfaceView surfaceView;
    CameraEngine engine;
    CameraRecord recorder;
    Button startBtn;
    Button stopBtn;
    byte[] byteBuffer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity_main);

        surfaceView = (SurfaceView) findViewById(R.id.surface);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().addCallback(this);

        startBtn = (Button) findViewById(R.id.start_button);
        startBtn.setOnClickListener(this);
        stopBtn = (Button) findViewById(R.id.stop_button);
        stopBtn.setOnClickListener(this);

        engine = CameraEngine.getInstance(this);
    }

    public void openCamera() {
        engine.openCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        engine.setOrientation(90);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Size size = engine.getCamera().getParameters().getPreviewSize();
        recorder = new CameraRecord(size.width, size.height);
        byteBuffer = new byte[size.width * size.height * 3 / 2];
        engine.addCallbackBuffer(byteBuffer);
        engine.setPreviewCallbackWithBuffer(this);
        engine.startPreview(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        engine.stopPreview();
        engine.releaseCamera();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_button) {
            recorder.prepareRecord();
        } else if (v.getId() == R.id.stop_button) {
            recorder.stopRecord();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (recorder.prepareRecord()) {
            recorder.startRecord();
            recorder.makeCurrent();
        } else {
            recorder.makeCurrent();
        }

        recorder.swapBuffers();
        engine.addCallbackBuffer(byteBuffer);
    }
}
