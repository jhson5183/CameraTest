package com.example.kakao.cameratest;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.kakao.cameratest.draw.Image;
import com.example.kakao.cameratest.draw.Triangle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by huey on 2018. 1. 2..
 */

public class GlDrawActivity extends AppCompatActivity{

    private GLSurfaceView glSurfaceView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glSurfaceView = new GLSurfaceView(this);

        glSurfaceView.setEGLContextClientVersion(2);

        DrawRenderer renderer = new DrawRenderer();

        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        setContentView(glSurfaceView);
    }

    class DrawRenderer implements GLSurfaceView.Renderer {

        private Triangle triangle;
        private Image image;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

            triangle = new Triangle();
            image = new Image(GlDrawActivity.this, R.drawable.ic_launcher);

        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            image.onSurfaceChanged(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

//            triangle.draw();

            image.draw(-1);

        }
    }

}
