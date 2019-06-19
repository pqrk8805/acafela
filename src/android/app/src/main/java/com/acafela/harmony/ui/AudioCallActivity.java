package com.acafela.harmony.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.acafela.harmony.R;
import com.acafela.harmony.service.HarmonyService;

import static com.acafela.harmony.ui.CallActivity.INTENT_CONTROL;
import static com.acafela.harmony.ui.CallActivity.INTENT_SIP_ACCEPT_CALL;
import static com.acafela.harmony.ui.CallActivity.INTENT_SIP_TERMINATE_CALL;

public class AudioCallActivity extends AppCompatActivity {
    private static final String TAG = AudioCallActivity.class.getName();

    public static final String INTENT_PHONENUMBER = "INTENT_PHONENUMBER";
    public static final String INTENT_ISRINGING = "INTENT_ISCALEE";

    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_audiocall);

        Intent intent = getIntent();

        TextView phoneNumberTextView = findViewById(R.id.tv_phonenumber);
        phoneNumberTextView.setText(intent.getStringExtra(INTENT_PHONENUMBER));

        boolean isRinging = intent.getBooleanExtra(INTENT_ISRINGING, false);
        if (isRinging) {
            findViewById(R.id.fourth_container).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.fourth_container_ringing).setVisibility(View.GONE);
        }

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        terminateCall();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
        if(((ToggleButton) v).isChecked()) {
            ((ToggleButton)findViewById(R.id.toggle_bluetooth)).setChecked(false);
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.stopBluetoothSco();
            mAudioManager.setSpeakerphoneOn(true);
        } else {
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.stopBluetoothSco();
            mAudioManager.setSpeakerphoneOn(false);
        }
    }

    public void onClickBluetoothToggleBtn(View v) {
        if(((ToggleButton) v).isChecked()) {
            if (isBluetoothConnected()) {
                ((ToggleButton) findViewById(R.id.toggle_speaker)).setChecked(false);
                mAudioManager.setSpeakerphoneOn(false);
                mAudioManager.setBluetoothScoOn(true);
                mAudioManager.startBluetoothSco();
            }
            else {
                ((ToggleButton)findViewById(R.id.toggle_bluetooth)).setChecked(false);
                Intent intentOpenBluetoothSettings = new Intent();
                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentOpenBluetoothSettings);
            }
        } else {
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.stopBluetoothSco();
            mAudioManager.setSpeakerphoneOn(false);
        }
    }

    private void terminateCall() {
        Intent serviceIntent = new Intent(getApplicationContext(), HarmonyService.class);
        serviceIntent.putExtra(INTENT_CONTROL, INTENT_SIP_TERMINATE_CALL);
        startService(serviceIntent);

        finish();
    }

    public static boolean isBluetoothConnected() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
                && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED;
    }
}