package com.acafela.harmony.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.media.AudioManager;

public class AudioPathSelector {
    private static final String TAG = AudioPathSelector.class.getName();

    private static AudioPathSelector INSTANCE;

    private static AudioManager mAudioManager;

    public synchronized static AudioPathSelector getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AudioPathSelector();
        }
        return INSTANCE;
    }

    public void setAudioManager(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void setSpeakerAudio() {
        mAudioManager.setBluetoothScoOn(false);
        mAudioManager.stopBluetoothSco();
        mAudioManager.setSpeakerphoneOn(true);
    }

    public void setEarPieceAudio() {
        mAudioManager.setBluetoothScoOn(false);
        mAudioManager.stopBluetoothSco();
        mAudioManager.setSpeakerphoneOn(false);
    }

    public void setBluetoothAudio() {
        mAudioManager.setSpeakerphoneOn(false);
        mAudioManager.setBluetoothScoOn(true);
        mAudioManager.startBluetoothSco();
    }

    public static boolean isBluetoothConnected() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
                && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED;
    }
}
