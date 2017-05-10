package com.diy.cheng.camera;

/**
 * Created by 0 on 2017/2/4.
 */

public interface CameraListener {
    public void openCameraFailed(String error);

    public void switchCameraFailed(String error);
}
