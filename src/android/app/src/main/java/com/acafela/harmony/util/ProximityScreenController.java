package com.acafela.harmony.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class ProximityScreenController implements SensorEventListener
{
    private static final String LOG_TAG = "ProxScr";

    private Context mContext;
    private Window mWindow;
    private Sensor mProximitySensor;

    public ProximityScreenController(Context context, Window window)
    {
        mContext = context;
        mWindow = window;
    }

    public void activate()
    {
        SensorManager sensorManager = (SensorManager)mContext.getSystemService(
                Context.SENSOR_SERVICE);
        mProximitySensor = sensorManager.getDefaultSensor(
                Sensor.TYPE_PROXIMITY);
        if (mProximitySensor == null) {
            Log.e(LOG_TAG, "ERROR: Proximity sensor not available.");
        }

        sensorManager.registerListener(
                this,
                mProximitySensor,
                1 * 1000 * 1000);

    }

    public void deactivate()
    {
        SensorManager sensorManager = (SensorManager)mContext.getSystemService(
                                                        Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d(LOG_TAG, sensorEvent.toString());
        if (sensorEvent.values[0] < mProximitySensor.getMaximumRange()) {
            Log.d(LOG_TAG, "detected something nearby");

            WindowManager.LayoutParams params = mWindow.getAttributes();
            params.screenBrightness = 0;
            mWindow.setAttributes(params);
/*
            PowerManager manager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
            @SuppressLint("InvalidWakeLockTag")
            PowerManager.WakeLock wl = manager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "HarmoyTag");
            if (wl.isHeld())
                wl.release();
*/
        } else {
            Log.d(LOG_TAG, "Nothing is nearby");
            WindowManager.LayoutParams params = mWindow.getAttributes();
            params.screenBrightness = 1;
            mWindow.setAttributes(params);
/*
            PowerManager manager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
            @SuppressLint("InvalidWakeLockTag")
            PowerManager.WakeLock wl = manager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "HarmoyTag");
            wl.acquire();
*/
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(LOG_TAG, sensor.toString());
        Log.d(LOG_TAG, String.valueOf(i));
    }
}
