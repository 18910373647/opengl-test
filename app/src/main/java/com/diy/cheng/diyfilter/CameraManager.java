package com.diy.cheng.diyfilter;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.io.IOException;

/**
 * Created by 0 on 2017/5/9.
 */

public class CameraManager {
    private static CameraManager instance = null;
    private Context context;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private boolean front = true;

    private Camera camera;

    private CameraManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static CameraManager getInstance(Context context) {
        if (instance == null) {
            synchronized (CameraManager.class) {
                if (instance == null) {
                    instance = new CameraManager(context);
                }
            }
        }
        return instance;
    }

    public boolean checkCameraDevices(boolean front) {
        int cameraCount = Camera.getNumberOfCameras();
        if (cameraCount <= 0) {
            return false;
        }

        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (front) {
               if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                   cameraId = i;
                   return true;
               }
            } else {
               if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                   cameraId = i;
                   return true;
               }
            }
        }
        return false;
    }

    public void openCamera(SurfaceTexture surfaceTexture) {
        checkCameraDevices(front);
        camera = Camera.open(cameraId);

        Camera.Parameters parameters = camera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        camera.setParameters(parameters);
        try {
            camera.setPreviewTexture(surfaceTexture);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
