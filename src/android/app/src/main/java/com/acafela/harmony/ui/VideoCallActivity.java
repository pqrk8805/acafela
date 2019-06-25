package com.acafela.harmony.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.acafela.harmony.R;
import com.acafela.harmony.service.HarmonyService;
import com.acafela.harmony.util.AudioPathSelector;

import static com.acafela.harmony.ui.AudioCallActivity.BROADCAST_BYE;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_CONTROL;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_SIP_ACCEPT_CALL;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_SIP_TERMINATE_CALL;

public class VideoCallActivity extends AppCompatActivity {
    private static final String TAG = VideoCallActivity.class.getName();

    BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_videocall);

        Intent intent = getIntent();

        AudioPathSelector.getInstance().setAudioManager(this);
        AudioPathSelector.getInstance().setEarPieceAudio();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RegisterReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        UnregisterReceiver();
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