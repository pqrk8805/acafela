package com.acafela.harmony.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;

public class ProximityScreenController
{
    private static final String LOG_TAG = "ProxScr";

    private Context mContext;

    public ProximityScreenController(Context context)
    {
        mContext = context;
    }

    public void activate()
    {
        PowerManager pm = (PowerManager)mContext.getSystemService(
                                                        Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag")
        PowerManager.WakeLock wl = pm.newWakeLock(
                                    PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                                    "HarmoyTag");
        wl.acquire();
    }

    public void deactivate()
    {
        PowerManager pm = (PowerManager)mContext.getSystemService(
                                                        Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag")
        PowerManager.WakeLock wl = pm.newWakeLock(
                                    PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                                    "HarmoyTag");
        if (wl.isHeld())
            wl.release();
    }
}
