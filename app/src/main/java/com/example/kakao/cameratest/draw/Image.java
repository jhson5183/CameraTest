package com.example.kakao.cameratest.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.example.kakao.cameratest.gles.Drawable2d;
import com.example.kakao.cameratest.gles.GeneratedTexture;
import com.example.kakao.cameratest.gles.GlUtil;
import com.example.kakao.cameratest.gles.Sprite2d;
import com.example.kakao.cameratest.gles.Texture2dProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * Created by huey on 2018. 1. 2..
 */

public class Image {

//    private final String vertexShaderCode =
//            "attribute vec4 vPosition;" +
//                    "void main(){" +
//                    " gl_Position = vPosition;" +
//                    "}";
//
//    private final String fragmentShaderCode =
//            "precision mediump float;" +
//                    "uniform vec4 vColor;" +
//                    "void main() {" +
//                    " gl_FragColor = vColor;" +
//                    "}";
//
//    static float triangleCoords[] = {
//            0.0f, 0.622008459f, 0.0f,
//            -0.5f, -0.311004243f, 0.0f,
//            0.5f, -0.311004243f, 0.0f
//    };
//
//    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
//
//    static final int COORDS_PER_VERTEX = 3;

    private final Drawable2d rectDrawable = new Drawable2d(Drawable2d.Prefab.RECTANGLE);
    private Sprite2d sprite2d;
    private Texture2dProgram texture2dProgram;
    private float[] mDisplayProjectionMatrix = new float[16];
    private int imageTexture;
    private static final int TEX_SIZE = 64;
    private static final int FORMAT = GLES20.GL_RGBA;

    public Image(Context context, int resId) {

//        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bitmap.getWidth() * bitmap.getHeight() * 4);
//        byteBuffer.order(ByteOrder.BIG_ENDIAN);
//        IntBuffer intBuffer = byteBuffer.asIntBuffer();

//        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
//        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
//        for (int i= 0; i < pixels.length; i++) {
//            intBuffer.put(pixels[i] << 8 | pixels[i] >>> 24);
//        }

//        byteBuffer.position(0);
        sprite2d = new Sprite2d(rectDrawable);

        texture2dProgram = new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_2D);
//        imageTexture = GeneratedTexture.createTestTexture(GeneratedTexture.Image.COARSE);
        imageTexture = loadTexture(context, resId);

//        sprite2d.setTexture(imageTexture);
//        sprite2d.setScale(1, 1);

    }

    public void onSurfaceChanged(int width, int height) {

        GLES20.glViewport(0, 0, width, height);

        Matrix.orthoM(mDisplayProjectionMatrix, 0, 0, width, 0, height, -1, 1);

        int smallDim = Math.min(width, height);

        sprite2d.setColor(0.1f, 0.9f, 0.1f);
        sprite2d.setTexture(imageTexture);
        sprite2d.setScale(smallDim / 3.0f, smallDim / 3.0f);
        sprite2d.setPosition(width / 2.0f, height / 2.0f);
    }

    public void draw(int imageTextureId) {
        if(imageTextureId != -1) {
            sprite2d.setTexture(imageTextureId);
        }
        Log.e("jhson", "draw imageTextureId : " + imageTextureId);
        sprite2d.draw(texture2dProgram, mDisplayProjectionMatrix);
    }

    public void draw(int imageTextureId, boolean isEncording) {
        if(imageTextureId != -1) {
            sprite2d.setTexture(imageTextureId);
        }
        Log.e("jhson", "isEncording draw imageTextureId : " + imageTextureId);
        sprite2d.draw(texture2dProgram, mDisplayProjectionMatrix);
    }

    private int loadTexture(Context context, int resId) {

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        int mTextureID = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);


        Matrix.orthoM(mDisplayProjectionMatrix, 0, 0, bitmap.getWidth(), 0, bitmap.getHeight(), -1, 1);

        bitmap.recycle();

        return mTextureID;
    }

    public void release(boolean doEglCleanup) {
        if (texture2dProgram != null) {
            if (doEglCleanup) {
                texture2dProgram.release();
            }
            texture2dProgram = null;
        }
    }

    public int getImageTexture() {
        return imageTexture;
    }
}
