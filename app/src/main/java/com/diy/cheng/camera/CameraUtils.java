package com.diy.cheng.camera;

import android.hardware.Camera;
import android.util.Log;

import java.util.List;

/**
 * Created by 0 on 2017/1/24.
 */

public class CameraUtils {
    public static Camera.Size getSupportLargePreviewSize(Camera camera, int width, int height) {
        List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
        Camera.Size size = null;
        for (int i = 0; i < sizes.size(); i++) {    // 从大到小排列
            if (i + 1 > sizes.size()) {
                size = sizes.get(i);
                break;
            }
            if (width <= sizes.get(i).width && width > sizes.get(i + 1).width) {
                size = sizes.get(i);
                break;
            }
        }
        return size;
    }

    public static Camera.Size getSupportLargePictureSize(Camera camera) {
        List<Camera.Size> sizes = camera.getParameters().getSupportedPictureSizes();
        Camera.Size tmp = sizes.get(0);
        for (int i = 0; i < sizes.size(); i++) {
            if (tmp.width < sizes.get(i).width) {
                tmp = sizes.get(i);
            }
        }
        return tmp;
    }

    public static boolean checkCameraId(int cameraId) {
        int camera_count = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < camera_count; i++) {
            Camera.getCameraInfo(cameraId, info);
            if (info.facing == cameraId) {
                return true;
            }
        }
        return false;
    }

    public static void getSupportCameraPreviewFormat(Camera camera) {
        if (camera == null) {
            return ;
        }

        Camera.Parameters parameters = camera.getParameters();
        List<Integer> supportList = parameters.getSupportedPictureFormats();
        for (int i = 0; i < supportList.size(); i++) {
            Log.e("chengqixiang", "format === " + supportList.get(i));
        }
    }
}
