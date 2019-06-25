package com.acafela.harmony.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ToggleButton;

import com.acafela.harmony.R;
import com.acafela.harmony.codec.video.TextureMovieEncoder;
import com.acafela.harmony.codec.video.VideoDecodeAsyncSurface;
import com.acafela.harmony.codec.video.VideoEncodeSyncSurface;
import com.acafela.harmony.codec.video.gles.FullFrameRect;
import com.acafela.harmony.codec.video.gles.Texture2dProgram;
import com.acafela.harmony.service.HarmonyService;
import com.acafela.harmony.ui.camera.CameraHandler;
import com.acafela.harmony.ui.camera.CameraUtils;
import com.acafela.harmony.util.AudioPathSelector;

import java.io.IOException;
import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_HEIGHT;
import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_WIDTH;
import static com.acafela.harmony.ui.AudioCallActivity.BROADCAST_BYE;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_CONTROL;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_SIP_ACCEPT_CALL;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_SIP_TERMINATE_CALL;

public class VideoCallActivity extends VideoSurfaceActivity {
    private static final String TAG = VideoCallActivity.class.getName();

    private BroadcastReceiver mBroadcastReceiver;
    private TextureView mTextureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActionBar().hide();
        setContentView(R.layout.activity_videocall);

        mVideoEncoder.setEncodeCallback(new VideoEncodeSyncSurface.VideoCallback() {
            @Override
            public void onOutputBytesAvailable(byte[] outputBytes) {
                Log.i(TAG, "EncodedBytes: " + outputBytes.length);
                mVideoDecoder.enqueueInputBytes(outputBytes);
            }
        });

        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(mGLView);

        mTextureView = findViewById(R.id.textureviewMain);
        mTextureView.setSurfaceTextureListener(this);

        AudioPathSelector.getInstance().setAudioManager(this);
        AudioPathSelector.getInstance().setSpeakerAudio();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraHandler.invalidateHandler();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onPause start");
        super.onResume();
        RegisterReceiver();
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
        UnregisterReceiver();
        mGLView.queueEvent(new Runnable() {
            @Override public void run() {
                // Tell the renderer that it's about to be paused so it can clean up.
                mRenderer.notifyPausing();
            }
        });
        mGLView.onPause();
        Log.d(TAG, "onPause complete");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        terminateCall();
        finish();
    }

    public void onClickAcceptCallBtn(View v) {
        Intent serviceIntent = new Intent(getApplicationContext(), HarmonyService.class);
        serviceIntent.putExtra(INTENT_CONTROL, INTENT_SIP_ACCEPT_CALL);
        startService(serviceIntent);

        findViewById(R.id.fourth_container).setVisibility(View.VISIBLE);
        findViewById(R.id.fourth_container_ringing).setVisibility(View.GONE);
    }

    public void onClickTerminateCallBtn(View v) {
        terminateCall();
    }

    private void terminateCall() {
        Intent serviceIntent = new Intent(getApplicationContext(), HarmonyService.class);
        serviceIntent.putExtra(INTENT_CONTROL, INTENT_SIP_TERMINATE_CALL);
        startService(serviceIntent);

        finish();
    }

    private void RegisterReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_BYE);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BROADCAST_BYE)) {
                    Log.i(TAG, "onReceive BYE");
                    finish();
                }
            }
        };
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void UnregisterReceiver() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }
}