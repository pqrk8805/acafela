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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.acafela.harmony.R;
import com.acafela.harmony.codec.video.VideoEncodeSyncSurface;
import com.acafela.harmony.communicator.VideoReceiverThread;
import com.acafela.harmony.communicator.VideoSenderThread;
import com.acafela.harmony.service.HarmonyService;
import com.acafela.harmony.ui.contacts.DatabaseHelper;
import com.acafela.harmony.util.AudioPathSelector;

import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_HEIGHT;
import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_WIDTH;
import static com.acafela.harmony.ui.AudioCallActivity.BROADCAST_BYE;
import static com.acafela.harmony.ui.AudioCallActivity.BROADCAST_RECEIVEVIDEO;
import static com.acafela.harmony.ui.AudioCallActivity.BROADCAST_SENDVIDEO;
import static com.acafela.harmony.ui.AudioCallActivity.INTENT_ISCALLEE;
import static com.acafela.harmony.ui.AudioCallActivity.INTENT_PHONENUMBER;
import static com.acafela.harmony.ui.AudioCallActivity.KEY_IP;
import static com.acafela.harmony.ui.AudioCallActivity.KEY_PORT;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_CONTROL;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_SIP_ACCEPT_CALL;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_SIP_TERMINATE_CALL;

public class VideoCallActivity extends VideoSurfaceActivity {
    private static final String TAG = VideoCallActivity.class.getName();

    private BroadcastReceiver mBroadcastReceiver;
    private TextureView mTextureView;
    private static VideoSenderThread mVideoSenderThread;
    private static VideoReceiverThread mVideoReceiverThread;

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
                if (mVideoSenderThread != null) {
                    mVideoSenderThread.enqueueFrame(outputBytes);
                }
            }
        });

        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(mGLView);

        mTextureView = findViewById(R.id.textureviewMain);
        mTextureView.setSurfaceTextureListener(this);

        Intent intent = getIntent();
        final String phoneNumber = intent.getStringExtra(INTENT_PHONENUMBER);
        TextView phoneNumberTextView = findViewById(R.id.tv_phonenumber);
        phoneNumberTextView.setText(phoneNumber);
        TextView nameTextView = findViewById(R.id.tv_name);
        nameTextView.setText(DatabaseHelper.createContactDatabaseHelper(this).query(phoneNumber));
        boolean isCallee = intent.getBooleanExtra(INTENT_ISCALLEE, false);
        if (isCallee) {
            findViewById(R.id.button_container).setVisibility(View.GONE);
            findViewById(R.id.button_container_callee).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.button_container).setVisibility(View.VISIBLE);
            findViewById(R.id.button_container_callee).setVisibility(View.GONE);
        }

        AudioPathSelector.getInstance().setAudioManager(this);
        AudioPathSelector.getInstance().setSpeakerAudio();
        ToggleButton speakerToggleBtn = findViewById(R.id.toggle_speaker);
        speakerToggleBtn.setChecked(true);
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
    }

    public void onClickAcceptCallBtn(View v) {
        Intent serviceIntent = new Intent(getApplicationContext(), HarmonyService.class);
        serviceIntent.putExtra(INTENT_CONTROL, INTENT_SIP_ACCEPT_CALL);
        startService(serviceIntent);

        findViewById(R.id.button_container).setVisibility(View.VISIBLE);
        findViewById(R.id.button_container_callee).setVisibility(View.GONE);
        findViewById(R.id.first_container).setVisibility(View.GONE);
    }

    public void onClickTerminateCallBtn(View v) {
        terminateCall();
    }

    private void terminateCall() {
        Log.i(TAG, "terminateCall");
        mGLView.queueEvent(new Runnable() {
            @Override public void run() {
                mRenderer.changeRecordingState(false);
            }
        });
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Intent serviceIntent = new Intent(getApplicationContext(), HarmonyService.class);
        serviceIntent.putExtra(INTENT_CONTROL, INTENT_SIP_TERMINATE_CALL);
        startService(serviceIntent);
        if (mVideoReceiverThread != null) {
            mVideoReceiverThread.kill();
            mVideoReceiverThread = null;
        }
        if (mVideoSenderThread != null) {
            mVideoSenderThread.kill();
            mVideoSenderThread = null;
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
                    terminateCall();
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
                    if (mVideoSenderThread == null) {
                        mVideoSenderThread = new VideoSenderThread();
                        mVideoSenderThread.setAddress(ip, port);
                        mVideoSenderThread.start();
                    }
                }
                else if (intent.getAction().equals(BROADCAST_RECEIVEVIDEO)) {
                    Log.i(TAG, "onReceive BROADCAST_RECEIVEVIDEO");
                    int port = intent.getIntExtra(KEY_PORT, 0);
                    if (mVideoReceiverThread == null) {
                        mVideoReceiverThread = new VideoReceiverThread();
                        mVideoReceiverThread.setDecoder(mVideoDecoder, port);
                        mVideoReceiverThread.start();
                    }
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


    public void onClickSpeakerToggleBtn(View v) {
        if (((ToggleButton) v).isChecked()) {
            ((ToggleButton) findViewById(R.id.toggle_bluetooth)).setChecked(false);
            AudioPathSelector.getInstance().setSpeakerAudio();
        } else {
            AudioPathSelector.getInstance().setEarPieceAudio();
        }
    }

    public void onClickBluetoothToggleBtn(View v) {
        if (((ToggleButton) v).isChecked()) {
            if (AudioPathSelector.isBluetoothConnected()) {
                ((ToggleButton) findViewById(R.id.toggle_speaker)).setChecked(false);
                AudioPathSelector.getInstance().setBluetoothAudio();
            } else {
                ((ToggleButton) findViewById(R.id.toggle_bluetooth)).setChecked(false);
                Toast.makeText(this, "Bluetooth Speaker is not Connected.", Toast.LENGTH_SHORT).show();
            }
        } else {
            AudioPathSelector.getInstance().setEarPieceAudio();
        }
    }
}