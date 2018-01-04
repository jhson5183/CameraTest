package com.example.kakao.cameratest.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import com.example.kakao.cameratest.R;
import com.example.kakao.cameratest.draw.Image;
import com.example.kakao.cameratest.draw.Triangle;
import com.example.kakao.cameratest.gles.Drawable2d;
import com.example.kakao.cameratest.gles.GlUtil;
import com.example.kakao.cameratest.gles.Texture2dProgram;
import com.example.kakao.cameratest.movie.TextureMovieEncoder;

import java.io.File;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by huey on 2018. 1. 2..
 */

public class CameraSurfaceRenderer implements GLSurfaceView.Renderer {

    private static final int RECORDING_OFF = 0;
    private static final int RECORDING_ON = 1;
    private static final int RECORDING_RESUMED = 2;

    private SurfaceTexture surfaceTexture;
    private int textureId;
    private int imageTextureId;
    private CameraActivity.CameraHandler cameraHandler;

    private boolean mIncomingSizeUpdated;
    private int mIncomingWidth;
    private int mIncomingHeight;
    private final float[] mSTMatrix = new float[16];
    private final Drawable2d mRectDrawable = new Drawable2d(Drawable2d.Prefab.FULL_RECTANGLE);
    private Context context;
    Texture2dProgram texture2dProgram;
    Triangle triangle;
    Image image;

    private TextureMovieEncoder textureMovieEncoder;
    private File outputFile;
    private boolean isRecordingEnabled;
    private int mRecordingStatus;

    public CameraSurfaceRenderer(Context context, CameraActivity.CameraHandler cameraHandler, TextureMovieEncoder textureMovieEncoder, File outputFile) {

        textureId = 1;
        this.cameraHandler = cameraHandler;
        this.context = context;
        this.textureMovieEncoder = textureMovieEncoder;
        this.outputFile = outputFile;
        isRecordingEnabled = false;
        mRecordingStatus = -1;
    }

    public void setCameraPreviewSize(int width, int height) {
        mIncomingWidth = width;
        mIncomingHeight = height;
        mIncomingSizeUpdated = true;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        isRecordingEnabled = textureMovieEncoder.isRecording();
        if(isRecordingEnabled) {
            mRecordingStatus = RECORDING_RESUMED;
        } else {
            mRecordingStatus = RECORDING_OFF;
        }

        texture2dProgram = new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT);
        textureId = texture2dProgram.createTextureObject();
        surfaceTexture = new SurfaceTexture(textureId);

//        triangle = new Triangle();

        image = new Image(context, R.drawable.ic_launcher);
        imageTextureId = image.getImageTexture();

        Log.e("jhson", "onSurfaceChanged textureId : " + textureId);

        cameraHandler.sendMessage(cameraHandler.obtainMessage(CameraActivity.CameraHandler.MSG_SET_SURFACE_TEXTURE, surfaceTexture));
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.e("jhson", "onSurfaceChanged " + width + "x" + height);
        image.onSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        surfaceTexture.updateTexImage();

        if(isRecordingEnabled) {
            switch (mRecordingStatus) {
                case RECORDING_OFF:
                    textureMovieEncoder.startRecording(new TextureMovieEncoder.EncoderConfig(outputFile, 640, 480, 1000000, EGL14.eglGetCurrentContext()));
                    mRecordingStatus = RECORDING_ON;
                    break;
                case RECORDING_RESUMED:
                    textureMovieEncoder.updateSharedContext(EGL14.eglGetCurrentContext());
                    mRecordingStatus = RECORDING_ON;
                    break;
                case RECORDING_ON:
                    break;
                default:
            }
        } else {
            switch (mRecordingStatus) {
                case RECORDING_ON:
                case RECORDING_RESUMED:
                    textureMovieEncoder.stopRecording();
                    mRecordingStatus = RECORDING_OFF;
                    break;
                case RECORDING_OFF:
                    break;
                default:
            }
        }

        textureMovieEncoder.setTextureId(textureId);
        textureMovieEncoder.setImageTextureId(imageTextureId);

        textureMovieEncoder.frameAvailable(surfaceTexture);

        if(mIncomingWidth <= 0 || mIncomingHeight <= 0) {
            return ;
        }

        surfaceTexture.getTransformMatrix(mSTMatrix);
        texture2dProgram.draw(GlUtil.IDENTITY_MATRIX, mRectDrawable.getVertexArray(), 0,
                mRectDrawable.getVertexCount(), mRectDrawable.getCoordsPerVertex(),
                mRectDrawable.getVertexStride(),
                mSTMatrix, mRectDrawable.getTexCoordArray(), textureId,
                mRectDrawable.getTexCoordStride());

//        triangle.draw();
        image.draw(imageTextureId);

    }

    private int loadTexture (int texId) {
        int[] textures = new int[1];
        if(texId == 0) {
            GLES20.glGenTextures(1, textures, 0);
        } else {
            textures[0] = texId;
        }

        int mTextureID = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

        return mTextureID;
    }

    public void setRecordingEnabled(boolean recordingEnabled) {
        isRecordingEnabled = recordingEnabled;
    }
}