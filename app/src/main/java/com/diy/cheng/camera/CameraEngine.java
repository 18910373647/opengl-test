package com.diy.cheng.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * Created by 0 on 2017/1/24.
 */

public class CameraEngine {
    Context context;
    private static CameraEngine instance = null;
    private int cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private Camera camera = null;
    private SurfaceHolder holder;
    private SurfaceTexture surfaceTexture;
    private CameraListener listener;

    private CameraEngine(Context context) {
        this.context = context;
    }

    public static CameraEngine getInstance(Context context) {
        if (instance == null) {
            synchronized (CameraEngine.class) {
                if (instance == null) {
                    instance = new CameraEngine(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public Camera getCamera() {
        return camera;
    }

    public void openCamera() {
        openCamera(cameraID);
    }

    public void openCamera(int cameraID) {
        if (camera == null) {
            try {
                if (CameraUtils.checkCameraId(cameraID)) {
                    this.cameraID = cameraID;
                    camera = Camera.open(cameraID);
                    setDefaultParameters();
                } else if (listener != null) {
                    listener.openCameraFailed("not support open camera");
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.openCameraFailed("not support open camera");
                }
            }
        }
    }

    public void switchCamera() {
        if (CameraUtils.checkCameraId(1- cameraID)) {
            releaseCamera();
            cameraID = 1 - cameraID;
            openCamera(cameraID);
            if (holder != null) {
                startPreview(holder);
            } else if (surfaceTexture != null) {
                startPreview(surfaceTexture);
            }
        } else if (listener != null) {
            listener.switchCameraFailed("not support switch camera");
        }
    }

    public void startPreview(SurfaceHolder holder) {
        if (camera != null) {
            try {
                camera.setPreviewDisplay(holder);
                this.holder = holder;
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startPreview(SurfaceTexture surfaceTexture) {
        if (camera != null) {
            try {
                camera.setPreviewTexture(surfaceTexture);
                this.surfaceTexture = surfaceTexture;
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopPreview() {
        if (camera != null) {
            camera.stopPreview();
        }
    }

    public void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void setDefaultParameters() {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size preview_size = CameraUtils.getSupportLargePreviewSize(camera);
            parameters.setPreviewSize(preview_size.width, preview_size.height);
            Camera.Size picture_size = CameraUtils.getSupportLargePictureSize(camera);
            parameters.setPictureSize(picture_size.width, picture_size.height);
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            parameters.setRotation(90);
            camera.setParameters(parameters);
            // TODO
        }
    }

    public void setOrientation(int orientation) {
        if (camera != null) {
            camera.setDisplayOrientation(orientation);
        }
    }

    public boolean isFront() {
        return cameraID == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    public void setCameraListener(CameraListener listener) {
        this.listener = listener;
    }

    public void addCallbackBuffer(byte[] buffer) {
        if (camera != null) {
            camera.addCallbackBuffer(buffer);
        }
    }

    public void setPreviewCallbackWithBuffer(Camera.PreviewCallback callback) {
        if (camera != null) {
            camera.setPreviewCallbackWithBuffer(callback);
        }
    }

    public void setPreviewCallback(Camera.PreviewCallback callback) {
        if (camera != null) {
            camera.setPreviewCallback(callback);
        }
    }
}
