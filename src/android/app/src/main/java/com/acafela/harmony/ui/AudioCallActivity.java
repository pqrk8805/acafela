package com.acafela.harmony.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.acafela.harmony.R;
import com.acafela.harmony.controller.VoipController.STATE;
import com.acafela.harmony.service.HarmonyService;
import com.acafela.harmony.ui.call.CallTimer;
import com.acafela.harmony.ui.call.InCallDateUtils;
import com.acafela.harmony.ui.contacts.ContactDbHelper;
import com.acafela.harmony.ui.dialpad.AnimUtils;
import com.acafela.harmony.util.AudioPathSelector;
import com.acafela.harmony.util.ProximityScreenController;

import static com.acafela.harmony.controller.VoipController.STATE.RINGING_STATE;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_CONTROL;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_SIP_ACCEPT_CALL;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_SIP_TERMINATE_CALL;

public class AudioCallActivity extends FullScreenActivity {
    private static final String TAG = AudioCallActivity.class.getName();

    public static final String INTENT_PHONENUMBER = "INTENT_PHONENUMBER";
    public static final String INTENT_ISCALLEE = "INTENT_ISCALEE";
    public static final String INTENT_ISCONFERENCECALL = "IINTENT_ISCONFERENCECALL";
    public static final String BROADCAST_BYE = "com.acafela.action.bye";
    public static final String BROADCAST_SENDVIDEO = "com.acafela.action.sendvideo";
    public static final String BROADCAST_RECEIVEVIDEO = "com.acafela.action.receivevideo";
    public static final String BROADCAST_CONNECTING = "com.acafela.action.connecting";
    public static final String KEY_IP = "KEY_IP";
    public static final String KEY_PORT = "KEY_PORT";
    private static final long CALL_TIME_UPDATE_INTERVAL_MS = 1000;

    BroadcastReceiver mBroadcastReceiver;

    private ProximityScreenController mProxiScrController;
    private CallTimer mCallTimer;
    private TextView mElapsedTime;
    private long mConnectTimeMillis;
    private STATE mState = RINGING_STATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audiocall);

        Intent intent = getIntent();
        final String phoneNumber = intent.getStringExtra(INTENT_PHONENUMBER);
        TextView phoneNumberTextView = findViewById(R.id.tv_yourphonenumber);
        phoneNumberTextView.setText(phoneNumber);
        TextView nameTextView = findViewById(R.id.tv_yourname);
        nameTextView.setText(ContactDbHelper.CreateHelper(this).query(phoneNumber));
        boolean isRinging = intent.getBooleanExtra(INTENT_ISCALLEE, false);
        if (isRinging) {
            findViewById(R.id.button_container).setVisibility(View.GONE);
        } else {
            findViewById(R.id.button_container_callee).setVisibility(View.GONE);
        }

        AudioPathSelector.getInstance().setAudioManager(this);
        AudioPathSelector.getInstance().setEarPieceAudio();

        mProxiScrController = new ProximityScreenController(this);
        mProxiScrController.activate();

        mElapsedTime = findViewById(R.id.elapsedTime);
        mState = RINGING_STATE;
        mCallTimer = new CallTimer(new Runnable() {
            @Override
            public void run() {
                updateCallTime();
            }
        });
        mCallTimer.start(CALL_TIME_UPDATE_INTERVAL_MS);
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
        mProxiScrController.activate();
        Log.i(TAG, "onResume complete");
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause start");
        super.onPause();
        UnregisterReceiver();
        mProxiScrController.deactivate();
        Log.i(TAG, "onResume complete");
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");
        super.onBackPressed();
        UnregisterReceiver();
        mProxiScrController.deactivate();
        terminateCall();
        finish();
    }

    public void updateCallTime() {
        if (mState == STATE.CONNECTING_STATE) {
            final long duration = System.currentTimeMillis() - mConnectTimeMillis;
            setCallElapsedTime(true, duration);
        }
    }

    public void setCallElapsedTime(boolean show, long duration) {
        if (show) {
            if (mElapsedTime.getVisibility() != View.VISIBLE) {
                AnimUtils.fadeIn(mElapsedTime, AnimUtils.DEFAULT_DURATION);
            }
            String callTimeElapsed = DateUtils.formatElapsedTime(duration / 1000);
            mElapsedTime.setText(callTimeElapsed);

            String durationDescription =
                    InCallDateUtils.formatDuration(this, duration);
            mElapsedTime.setContentDescription(
                    !TextUtils.isEmpty(durationDescription) ? durationDescription : null);
        } else {
            // hide() animation has no effect if it is already hidden.
            AnimUtils.fadeOut(mElapsedTime, AnimUtils.DEFAULT_DURATION);
        }
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
                Intent intentOpenBluetoothSettings = new Intent();
                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentOpenBluetoothSettings);
            }
        } else {
            AudioPathSelector.getInstance().setEarPieceAudio();
        }
    }

    private void terminateCall() {
        mCallTimer.cancel();

        Intent serviceIntent = new Intent(getApplicationContext(), HarmonyService.class);
        serviceIntent.putExtra(INTENT_CONTROL, INTENT_SIP_TERMINATE_CALL);
        startService(serviceIntent);

        finish();
    }

    private void RegisterReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_BYE);
        intentFilter.addAction(BROADCAST_CONNECTING);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BROADCAST_BYE)) {
                    Log.i(TAG, "onReceive BYE");
                    terminateCall();
                }
                else if (intent.getAction().equals(BROADCAST_CONNECTING)) {
                    Log.i(TAG, "onReceive CONNECTING");
                    mConnectTimeMillis = System.currentTimeMillis();
                    mState = STATE.CONNECTING_STATE;
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
