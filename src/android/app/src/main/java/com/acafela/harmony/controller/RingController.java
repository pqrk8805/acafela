package com.acafela.harmony.controller;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.util.Log;

public class RingController {
    private static final String LOG_TAG = "[AcafelaRingController]";
    private Context mContext;
    Ringtone mRingtone = null;
    ToneGenerator mToneGenerator = null;

    public RingController(Context context)
    {
        mContext = context;
    }

    public void ring_start() {
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mRingtone = RingtoneManager.getRingtone(mContext, ringtoneUri);
        Log.e(LOG_TAG, "ringtone play: ");
        mRingtone.setLooping(true);
        mRingtone.play();
    }
    public void ring_stop() {
        if(mRingtone!=null) mRingtone.stop();
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
