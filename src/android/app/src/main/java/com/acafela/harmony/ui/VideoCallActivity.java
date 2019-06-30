package com.acafela.harmony.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.acafela.harmony.R;
import com.acafela.harmony.codec.video.VideoDecodeAsyncSurface;
import com.acafela.harmony.codec.video.VideoMediaFormat;
import com.acafela.harmony.communicator.VideoReceiverThread;
import com.acafela.harmony.communicator.VideoSenderThread;
import com.acafela.harmony.service.HarmonyService;
import com.acafela.harmony.ui.contacts.ContactDbHelper;
import com.acafela.harmony.util.AudioPathSelector;

import java.util.ArrayList;
import java.util.Arrays;

import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_HEIGHT_HIGHQ;
import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_HEIGHT_LOWQ;
import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_WIDTH_HIGHQ;
import static com.acafela.harmony.codec.video.VideoMediaFormat.VIDEO_WIDTH_LOWQ;
import static com.acafela.harmony.ui.AudioCallActivity.BROADCAST_BYE;
import static com.acafela.harmony.ui.AudioCallActivity.BROADCAST_RECEIVEVIDEO;
import static com.acafela.harmony.ui.AudioCallActivity.BROADCAST_SENDVIDEO;
import static com.acafela.harmony.ui.AudioCallActivity.INTENT_ISCALLEE;
import static com.acafela.harmony.ui.AudioCallActivity.INTENT_ISCONFERENCECALL;
import static com.acafela.harmony.ui.AudioCallActivity.INTENT_PHONENUMBER;
import static com.acafela.harmony.ui.AudioCallActivity.KEY_IP;
import static com.acafela.harmony.ui.AudioCallActivity.KEY_PORT;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_CONTROL;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_SIP_ACCEPT_CALL;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_SIP_TERMINATE_CALL;

public class VideoCallActivity extends CameraSenderActivity
        implements TextureView.SurfaceTextureListener {
    private static final String TAG = VideoCallActivity.class.getName();

    private BroadcastReceiver mBroadcastReceiver;
    private static ArrayList<VideoReceiverThread> mVideoReceiverThreadList = new ArrayList<>();
    protected ArrayList<VideoDecodeAsyncSurface> mVideoDecoderList = new ArrayList<>();
    private ArrayList<Integer> mPortArray = new ArrayList<>();
    private static int mAttctedViewCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate start");

        Intent intent = getIntent();
        boolean isConferenceCall = intent.getBooleanExtra(INTENT_ISCONFERENCECALL, false);
        if (isConferenceCall) {
            VideoMediaFormat.getInstance().setHighQuality(false);
        }
        else {
            VideoMediaFormat.getInstance().setHighQuality(true);
        }

        super.onCreate(savedInstanceState);

        mAttctedViewCount = 0;
        mVideoReceiverThreadList.clear();
        mVideoDecoderList.clear();
        mPortArray.clear();

        if (isConferenceCall) {
            setContentView(R.layout.activity_videocall_conference);
            {
                TextureView textureView = findViewById(R.id.yourview1);
                textureView.setSurfaceTextureListener(this); }
            {
                TextureView textureView = findViewById(R.id.yourview2);
                textureView.setSurfaceTextureListener(this); }
            {
                TextureView textureView = findViewById(R.id.yourview3);
                textureView.setSurfaceTextureListener(this); }
        }
        else {
            setContentView(R.layout.activity_videocall);
            TextureView textureView = findViewById(R.id.yourview);
            textureView.setSurfaceTextureListener(this);
        }
        FrameLayout preview = findViewById(R.id.myview);
        preview.addView(mGLView);

        initUi();
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
        Log.d(TAG, "onPause complete");
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");
        super.onBackPressed();
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
        for (VideoReceiverThread thread : mVideoReceiverThreadList) {
            thread.kill();
        }
        mVideoReceiverThreadList.clear();
        for (VideoDecodeAsyncSurface decoder : mVideoDecoderList) {
            decoder.stop();
        }
        mVideoDecoderList.clear();
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
                            mRenderer.setCameraPreviewSize(
                                    VideoMediaFormat.getInstance().getWidth(),
                                    VideoMediaFormat.getInstance().getHeight());
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
                    hideTextView();
                }
                else if (intent.getAction().equals(BROADCAST_RECEIVEVIDEO)) {
                    int port = intent.getIntExtra(KEY_PORT, 0);
                    Log.i(TAG, "onReceive BROADCAST_RECEIVEVIDEO: " + port);
                    Log.i(TAG, "mPortArray: " + Arrays.toString(mPortArray.toArray()));
                    if (mPortArray.contains(mPortArray)) {
                        Log.e(TAG, "mPortArray has already " + port);
                        return;
                    }
                    Log.i(TAG, "new VideoReceiverThread");
                    VideoReceiverThread thread = new VideoReceiverThread();
                    thread.setDecoder(mVideoDecoderList.get(mAttctedViewCount), port);
                    mAttctedViewCount++;
                    mPortArray.add(port);
                    thread.start();
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

    private void hideTextView() {
        findViewById(R.id.uhdcalltext_container).setVisibility(View.GONE);
        findViewById(R.id.callstate_container).setVisibility(View.GONE);
        findViewById(R.id.yourinfo_container).setVisibility(View.GONE);
    }

    protected void initUi() {
        Intent intent = getIntent();
        final String phoneNumber = intent.getStringExtra(INTENT_PHONENUMBER);
        TextView phoneNumberTextView = findViewById(R.id.tv_yourphonenumber);
        phoneNumberTextView.setText(phoneNumber);
        TextView nameTextView = findViewById(R.id.tv_yourname);
        nameTextView.setText(ContactDbHelper.CreateHelper(this).query(phoneNumber));
        boolean isCallee = intent.getBooleanExtra(INTENT_ISCALLEE, false);
        if (isCallee) {
            findViewById(R.id.button_container).setVisibility(View.GONE);
            findViewById(R.id.button_container_callee).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.button_container).setVisibility(View.VISIBLE);
            findViewById(R.id.button_container_callee).setVisibility(View.GONE);
        }

//        AudioPathSelector.getInstance().setAudioManager(this);
//        AudioPathSelector.getInstance().setSpeakerAudio();
//        ToggleButton speakerToggleBtn = findViewById(R.id.toggle_speaker);
//        speakerToggleBtn.setChecked(true);

        AudioPathSelector.getInstance().setAudioManager(this);
        AudioPathSelector.getInstance().setEarPieceAudio();
        ToggleButton speakerToggleBtn = findViewById(R.id.toggle_speaker);
        speakerToggleBtn.setChecked(false);
    }


    public void onClickAcceptCallBtn(View v) {
        Intent serviceIntent = new Intent(getApplicationContext(), HarmonyService.class);
        serviceIntent.putExtra(INTENT_CONTROL, INTENT_SIP_ACCEPT_CALL);
        startService(serviceIntent);

        findViewById(R.id.button_container).setVisibility(View.VISIBLE);
        findViewById(R.id.button_container_callee).setVisibility(View.GONE);
        findViewById(R.id.uhdcalltext_container).setVisibility(View.GONE);
    }

    public void onClickTerminateCallBtn(View v) {
        terminateCall();
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

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureAvailable");
        Surface s = new Surface(surface);
        VideoDecodeAsyncSurface decoder = new VideoDecodeAsyncSurface(s);
        decoder.start();
        mVideoDecoderList.add(decoder);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
}
