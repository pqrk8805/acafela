package com.acafela.harmony.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.acafela.harmony.R;
import com.acafela.harmony.codec.video.VideoEncodeSyncSurface;

import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_HEIGHT;
import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_WIDTH;

public class TestVideoEncodingSurfaceActivity extends VideoSurfaceActivity {
    private static final String TAG = TestVideoEncodingSurfaceActivity.class.getName();

    private boolean mRecordingEnabled;      // controls button state
    private TextureView mTextureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_video_encoding_surface);

        mVideoEncoder.setEncodeCallback(new VideoEncodeSyncSurface.VideoCallback() {
            @Override
            public void onOutputBytesAvailable(byte[] outputBytes) {
                Log.i(TAG, "EncodedBytes: " + outputBytes.length);
                mVideoDecoder.enqueueInputBytes(outputBytes);
            }
        });

        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(mGLView);

        mTextureView = findViewById(R.id.textureview);
        mTextureView.setSurfaceTextureListener(this);

        Log.d(TAG, "onCreate complete");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume start");
        super.onResume();
        updateControls();

        mGLView.onResume();
        mGLView.queueEvent(new Runnable() {
            @Override public void run() {
                mRenderer.setCameraPreviewSize(VIDEO_WIDTH, VIDEO_HEIGHT);
            }
        });
        mGLView.queueEvent(new Runnable() {
            @Override public void run() {
                // notify the renderer that we want to change the encoder's state
                mRenderer.changeRecordingState(true);
            }
        });
        Log.d(TAG, "onResume complete");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause start");
        super.onPause();
        mGLView.queueEvent(new Runnable() {
            @Override public void run() {
                // Tell the renderer that it's about to be paused so it can clean up.
                mRenderer.notifyPausing();
            }
        });
        mGLView.onPause();
        Log.d(TAG, "onPause complete");
    }

    /**
     * onClick handler for "record" button.
     */
    public void clickToggleRecording(@SuppressWarnings("unused") View unused) {
        mRecordingEnabled = !mRecordingEnabled;
        mGLView.queueEvent(new Runnable() {
            @Override public void run() {
                // notify the renderer that we want to change the encoder's state
                mRenderer.changeRecordingState(mRecordingEnabled);
            }
        });
        updateControls();
    }

    /**
     * Updates the on-screen controls to reflect the current state of the app.
     */
    private void updateControls() {
        Button toggleRelease = findViewById(R.id.toggleRecording_button);
        if (mRecordingEnabled) {
            toggleRelease.setText("ON");
        }
        else {
            toggleRelease.setText("OFF");
        }
    }
}

