package com.acafela.harmony.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class ProximityScreenController
{
    private static final String LOG_TAG = "ProxScr";

    private Context mContext;
    @SuppressLint("InvalidWakeLockTag")
    PowerManager.WakeLock mWakeLock;

    public ProximityScreenController(Context context)
    {
        mContext = context;

        PowerManager pm = (PowerManager)mContext.getSystemService(
                                                        Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag")
        PowerManager.WakeLock wl = pm.newWakeLock(
                                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                                "HarmonyTag");
        mWakeLock = wl;
    }

    public void activate()
    {
        Log.i(LOG_TAG, "activate()");
        /*
        PowerManager pm = (PowerManager)mContext.getSystemService(
                                                        Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag")
        PowerManager.WakeLock wl = pm.newWakeLock(
                                    PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                                    "HarmonyTag");
        */
        if (mWakeLock.isHeld() == false)
            mWakeLock.acquire();
    }

    public void deactivate()
    {
        Log.i(LOG_TAG, "deactivate()");
        /*
        PowerManager pm = (PowerManager)mContext.getSystemService(
                                                        Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag")
        PowerManager.WakeLock wl = pm.newWakeLock(
                                    PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                                    "HarmonyTag");
        */
        if (mWakeLock.isHeld())
            mWakeLock.release();
    }
}
