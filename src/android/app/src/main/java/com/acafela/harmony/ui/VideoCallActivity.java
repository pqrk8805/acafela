package com.acafela.harmony.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import com.acafela.harmony.R;
import com.acafela.harmony.codec.video.VideoEncodeSyncSurface;
import com.acafela.harmony.communicator.VideoReceiverThread;
import com.acafela.harmony.communicator.VideoSender;
import com.acafela.harmony.service.HarmonyService;
import com.acafela.harmony.util.AudioPathSelector;

import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_HEIGHT;
import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_WIDTH;
import static com.acafela.harmony.ui.AudioCallActivity.BROADCAST_BYE;
import static com.acafela.harmony.ui.AudioCallActivity.BROADCAST_RECEIVEVIDEO;
import static com.acafela.harmony.ui.AudioCallActivity.BROADCAST_SENDVIDEO;
import static com.acafela.harmony.ui.AudioCallActivity.INTENT_ISCALLEE;
import static com.acafela.harmony.ui.AudioCallActivity.KEY_IP;
import static com.acafela.harmony.ui.AudioCallActivity.KEY_PORT;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_CONTROL;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_SIP_ACCEPT_CALL;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_SIP_TERMINATE_CALL;

public class VideoCallActivity extends VideoSurfaceActivity {
    private static final String TAG = VideoCallActivity.class.getName();

    private BroadcastReceiver mBroadcastReceiver;
    private TextureView mTextureView;
    private VideoSender mVideoSender = new VideoSender();
    private VideoReceiverThread mVideoReceiverThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videocall);

        mVideoEncoder.setEncodeCallback(new VideoEncodeSyncSurface.VideoCallback() {
            @Override
            public void onOutputBytesAvailable(byte[] outputBytes) {
                if (outputBytes == null) {
                    return;
                }
//                Log.d(TAG, "EncodedBytes: " + outputBytes.length);
                mVideoSender.sendFrame(outputBytes);
            }
        });

        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(mGLView);

        mTextureView = findViewById(R.id.textureviewMain);
        mTextureView.setSurfaceTextureListener(this);

        Intent intent = getIntent();
        boolean isRinging = intent.getBooleanExtra(INTENT_ISCALLEE, false);
        if (isRinging) {
            findViewById(R.id.button_container).setVisibility(View.GONE);
        } else {
            findViewById(R.id.button_container_callee).setVisibility(View.GONE);
        }

        AudioPathSelector.getInstance().setAudioManager(this);
        AudioPathSelector.getInstance().setSpeakerAudio();
        Log.d(TAG, "onCreate complete");
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume start");
        super.onResume();
        RegisterReceiver();
        Log.i(TAG, "onResume complete");
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause start");
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
        Log.i(TAG, "onBackPressed");
        super.onBackPressed();
        terminateCall();
        finish();
    }

    public void onClickAcceptCallBtn(View v) {
        Intent serviceIntent = new Intent(getApplicationContext(), HarmonyService.class);
        serviceIntent.putExtra(INTENT_CONTROL, INTENT_SIP_ACCEPT_CALL);
        startService(serviceIntent);

        findViewById(R.id.button_container).setVisibility(View.VISIBLE);
        findViewById(R.id.button_container_callee).setVisibility(View.GONE);
    }

    public void onClickTerminateCallBtn(View v) {
        terminateCall();
    }

    private void terminateCall() {
        Log.i(TAG, "terminateCall");
        Intent serviceIntent = new Intent(getApplicationContext(), HarmonyService.class);
        serviceIntent.putExtra(INTENT_CONTROL, INTENT_SIP_TERMINATE_CALL);
        startService(serviceIntent);
        if (mVideoReceiverThread != null) {
            mVideoReceiverThread.kill();
            mVideoReceiverThread = null;
        }

        finish();
    }

    private void RegisterReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_BYE);
        intentFilter.addAction(BROADCAST_SENDVIDEO);
        intentFilter.addAction(BROADCAST_RECEIVEVIDEO);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BROADCAST_BYE)) {
                    Log.i(TAG, "onReceive BROADCAST_BYE");
                    finish();
                }
                else if (intent.getAction().equals(BROADCAST_SENDVIDEO)) {
                    Log.i(TAG, "onReceive BROADCAST_SENDVIDEO");

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

                    String ip = intent.getStringExtra(KEY_IP);
                    int port = intent.getIntExtra(KEY_PORT, 0);
                    mVideoSender.start(ip, port);
                }
                else if (intent.getAction().equals(BROADCAST_RECEIVEVIDEO)) {
                    Log.i(TAG, "onReceive BROADCAST_RECEIVEVIDEO");
                    int port = intent.getIntExtra(KEY_PORT, 0);
                    mVideoReceiverThread = new VideoReceiverThread(mVideoDecoder, port);
                    mVideoReceiverThread.start();
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