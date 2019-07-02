package com.acafela.harmony.controller;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

public class RingController {
    private static final String LOG_TAG = "[AcafelaRingController]";
    private Context mContext;
    Ringtone mRingtone = null;
    ToneGenerator mToneGenerator = null;
    private Vibrator mVibrator;
    private int mMode = AudioManager.RINGER_MODE_NORMAL;
    int dot = 200;          // Length of a Morse Code "dot" in milliseconds
    int dash = 500;         // Length of a Morse Code "dash" in milliseconds
    int short_gap = 200;    // Length of Gap Between dots/dashes
    int medium_gap = 500;   // Length of Gap Between Letters
    int long_gap = 1000;    // Length of Gap Between Words
    long[] pattern = {
            0,  // Start immediately
            dot, short_gap, dot, short_gap, dot, medium_gap,    // S
            dash, short_gap, dash, short_gap, dash, medium_gap, // O
            dot, short_gap, dot, short_gap, dot, long_gap       // S
    };

    public RingController(Context context)
    {
        mContext = context;
    }

    public void ring_start() {
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        switch (am.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                Log.i(LOG_TAG,"Silent mode");
                mMode = AudioManager.RINGER_MODE_SILENT;
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                Log.i(LOG_TAG,"Vibrate mode");
                mMode = AudioManager.RINGER_MODE_VIBRATE;
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                Log.i(LOG_TAG,"Normal mode");
                mMode = AudioManager.RINGER_MODE_NORMAL;
                break;
        }

        if (mMode == AudioManager.RINGER_MODE_NORMAL) {
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mRingtone = RingtoneManager.getRingtone(mContext, ringtoneUri);
            Log.e(LOG_TAG, "ringtone play: ");
            mRingtone.setLooping(true);
            mRingtone.play();
        }
        else if (mMode == AudioManager.RINGER_MODE_VIBRATE) {
            mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            mVibrator.vibrate(pattern, 0);
        }
    }
    public void ring_stop() {
        if (mMode == AudioManager.RINGER_MODE_NORMAL) {
            if(mRingtone!=null) {
                mRingtone.stop();
            }
        }
        else if (mMode == AudioManager.RINGER_MODE_VIBRATE) {
            if(mVibrator!=null) {
                mVibrator.cancel();
            }
        }
    }

    public void ringbackTone_start() {
        int stream = AudioManager.STREAM_VOICE_CALL;

        try {
            mToneGenerator = new ToneGenerator(stream,80);
            //InCallTonePlayer.TONE_RELATIVE_VOLUME_HIPRI);
        } catch (RuntimeException e) {
            // If mToneGenerator creation fails, just continue without it.
            // It is a local audio signal, and is not as important.
            Log.w(LOG_TAG,
                    "InCallRingbackTonePlayer: Exception caught while creating ToneGenerator: " + e);
            mToneGenerator = null;
        }

        if (mToneGenerator != null) {
            mToneGenerator.startTone(ToneGenerator.TONE_SUP_RINGTONE);
        }
    }
    public void ringbackTone_stop() {
        if (mToneGenerator != null) {
            mToneGenerator.stopTone();
            mToneGenerator.release();
            mToneGenerator = null;
        }
    }
    public void allStop()
    {
        ringbackTone_stop();
        ring_stop();
    }
}
