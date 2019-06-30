package com.acafela.harmony.ui;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.acafela.harmony.codec.video.CameraSurfaceRenderer;
import com.acafela.harmony.codec.video.TextureMovieEncoder;
import com.acafela.harmony.codec.video.VideoEncodeSyncSurface;
import com.acafela.harmony.codec.video.VideoMediaFormat;
import com.acafela.harmony.communicator.VideoSenderThread;
import com.acafela.harmony.ui.camera.CameraHandler;
import com.acafela.harmony.ui.camera.CameraUtils;

import java.io.IOException;

import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_HEIGHT_HIGHQ;
import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_HEIGHT_LOWQ;
import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_WIDTH_HIGHQ;
import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_WIDTH_LOWQ;

public class CameraSenderActivity extends FullScreenActivity
        implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = CameraSenderActivity.class.getName();

    protected GLSurfaceView mGLView;
    protected CameraSurfaceRenderer mRenderer;
    protected CameraHandler mCameraHandler;
    protected static VideoSenderThread mVideoSenderThread;
    protected static TextureMovieEncoder mVideoEncoder = new TextureMovieEncoder();
    protected Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCameraHandler = new CameraHandler(this);
        mGLView = new GLSurfaceView(this);
        mGLView.setEGLContextClientVersion(2);     // select GLES 2.0
        mRenderer = new CameraSurfaceRenderer(mCameraHandler, mVideoEncoder);
        mGLView.setRenderer(mRenderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mVideoEncoder.setEncodeCallback(new VideoEncodeSyncSurface.VideoCallback() {
            @Override
            public void onOutputBytesAvailable(byte[] outputBytes) {
                if (outputBytes == null) {
                    return;
                }
//                Log.d(TAG, "EncodedBytes: " + outputBytes.length);
                if (mVideoSenderThread != null) {
                    mVideoSenderThread.enqueueFrame(outputBytes);
                }
            }
        });
        if (mVideoSenderThread != null) {
            mVideoSenderThread.kill();
            mVideoSenderThread = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraHandler.invalidateHandler();
    }

    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            openCamera(
                    VideoMediaFormat.getInstance().getWidth(),
                    VideoMediaFormat.getInstance().getHeight());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        mGLView.queueEvent(new Runnable() {
            @Override public void run() {
                // Tell the renderer that it's about to be paused so it can clean up.
                mRenderer.notifyPausing();
            }
        });
        mGLView.onPause();
    }

    /**
     * Opens a camera, and attempts to establish preview mode at the specified width and height.
     * <p>
     * Sets mCameraPreviewWidth and mCameraPreviewHeight to the actual width/height of the preview.
     */
    private void openCamera(int desiredWidth, int desiredHeight) {
        if (mCamera != null) {
            throw new RuntimeException("camera already initialized");
        }

        Camera.CameraInfo info = new Camera.CameraInfo();

        // Try to find a front-facing camera (e.g. for videoconferencing).
        int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCamera = Camera.open(i);
                break;
            }
        }
        if (mCamera == null) {
            Log.d(TAG, "No front-facing camera found; opening default");
            mCamera = Camera.open();    // opens first back-facing camera
        }
        if (mCamera == null) {
            throw new RuntimeException("Unable to open camera");
        }

        Camera.Parameters parms = mCamera.getParameters();

        CameraUtils.choosePreviewSize(parms, desiredWidth, desiredHeight);

        // Give the camera a hint that we're recording video.  This can have a big
        // impact on frame rate.
        parms.setRecordingHint(true);

        // leave the frame rate set to default
        mCamera.setParameters(parms);

        Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if(display.getRotation() == Surface.ROTATION_0) {
            mCamera.setDisplayOrientation(90);
        } else if(display.getRotation() == Surface.ROTATION_270) {
            mCamera.setDisplayOrientation(180);
        } else {
        }
    }

    /**
     * Stops camera preview, and releases the camera to the system.
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            Log.d(TAG, "releaseCamera -- done");
        }
    }

    /**
     * Connects the SurfaceTexture to the Camera preview output, and starts the preview.
     */
    public void handleSetSurfaceTexture(SurfaceTexture st) {
        st.setOnFrameAvailableListener(this);
        try {
            mCamera.setPreviewTexture(st);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        mCamera.startPreview();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture st) {
        // The SurfaceTexture uses this to signal the availability of a new frame.  The
        // thread that "owns" the external texture associated with the SurfaceTexture (which,
        // by virtue of the context being shared, *should* be either one) needs to call
        // updateTexImage() to latch the buffer.
        //
        // Once the buffer is latched, the GLSurfaceView thread can signal the encoder thread.
        // This feels backward -- we want recording to be prioritized over rendering -- but
        // since recording is only enabled some of the time it's easier to do it this way.
        //
        // Since GLSurfaceView doesn't establish a Looper, this will *probably* execute on
        // the main UI thread.  Fortunately, requestRender() can be called from any thread,
        // so it doesn't really matter.
        mGLView.requestRender();
    }
}

