package com.example.kakao.cameratest.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.kakao.cameratest.R;
import com.example.kakao.cameratest.movie.TextureMovieEncoder;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by huey on 2018. 1. 2..
 */

public class CameraActivity extends AppCompatActivity implements SurfaceTexture.OnFrameAvailableListener {

    private Camera camera;
    private GLSurfaceView glSurfaceView;
    private int cameraPreviewWidth;
    private int cameraPreviewHeight;

    private CameraSurfaceRenderer renderer;
    private CameraHandler cameraHandler;
    private boolean isRecordingEnabled;

    private TextureMovieEncoder textureMovieEncoder = new TextureMovieEncoder(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);

        glSurfaceView = findViewById(R.id.cameraPreview_surfaceView);
        glSurfaceView.setEGLContextClientVersion(2);

        isRecordingEnabled = textureMovieEncoder.isRecording();

        cameraHandler = new CameraHandler(this);
        renderer = new CameraSurfaceRenderer(this, cameraHandler, textureMovieEncoder, new File(Environment.getExternalStorageDirectory(), "camera-test.mp4"));
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        final Button encordingBtn = findViewById(R.id.camera_encording_btn);
        encordingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecordingEnabled = !isRecordingEnabled;
                glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        renderer.setRecordingEnabled(isRecordingEnabled);
                    }
                });
                encordingBtn.setText(isRecordingEnabled ? "encording stop" : "encording start");
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        openCamera(1280, 720);

        glSurfaceView.onResume();
        glSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                renderer.setCameraPreviewSize(cameraPreviewWidth, cameraPreviewHeight);
            }
        });

        ViewGroup.LayoutParams params = glSurfaceView.getLayoutParams();
        if(params != null) {
            params.width = cameraPreviewWidth;
            params.height = cameraPreviewHeight;
            glSurfaceView.setLayoutParams(params);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        glSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraHandler.invalidateHandler();
    }

    private void openCamera(int width, int height) {
        if(camera != null) {
            return ;
        }

        Camera.CameraInfo info = new Camera.CameraInfo();

        int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                camera = Camera.open(i);
                break;
            }
        }

        if(camera == null) {
            camera = Camera.open();
        }

        Camera.Parameters params = camera.getParameters();
        CameraUtils.choosePreviewSize(params, width, height);

        params.setRecordingHint(true);

        camera.setParameters(params);

        int[] fpsRange = new int[2];
        Camera.Size cameraPreviewSize = params.getPreviewSize();
        params.getPreviewFpsRange(fpsRange);
        String previewFacts = cameraPreviewSize.width + "x" + cameraPreviewSize.height;
        if(fpsRange[0] == fpsRange[1]) {
            previewFacts += " @" + (fpsRange[0] / 1000.0) + "fps";
        } else {
            previewFacts += " @[" + (fpsRange[0] / 1000.0) + " - " + (fpsRange[1] / 1000.0) + "] fps";
        }
        Log.e("jhson", "previewFacts : " + previewFacts);

        cameraPreviewWidth = cameraPreviewSize.width;
        cameraPreviewHeight = cameraPreviewSize.height;
    }

    private void releaseCamera() {
        if(camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        glSurfaceView.requestRender();
    }


    private void handleSetSurfaceTexture(SurfaceTexture surfaceTexture) {
        surfaceTexture.setOnFrameAvailableListener(this);
        try {
            camera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();

    }

    static class CameraHandler extends Handler {
        public static final int MSG_SET_SURFACE_TEXTURE = 0;

        private WeakReference<CameraActivity> weakReference;

        public CameraHandler(CameraActivity activity) {
            weakReference = new WeakReference<CameraActivity>(activity);
        }

        public void invalidateHandler() {
            weakReference.clear();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SET_SURFACE_TEXTURE:
                    CameraActivity activity = weakReference.get();
                    activity.handleSetSurfaceTexture((SurfaceTexture) msg.obj);
                    break;
            }
        }
    }
}
