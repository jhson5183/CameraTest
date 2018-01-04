package com.example.kakao.cameratest.camera;

import android.hardware.Camera;

/**
 * Created by huey on 2017. 12. 28..
 */

public class CameraUtils {

    public static void choosePreviewSize(Camera.Parameters parms, int width, int height) {
        Camera.Size ppsfv = parms.getPreferredPreviewSizeForVideo();
        if(ppsfv != null) {

        }

        for (Camera.Size size : parms.getSupportedPreviewSizes()) {
            if(size.width == width && size.height == height) {
                parms.setPreviewSize(width, height);
                return ;
            }
        }

        if(ppsfv != null) {
            parms.setPreviewSize(ppsfv.width, ppsfv.height);
        }
    }

}
