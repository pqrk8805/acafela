package com.acafela.harmony.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.acafela.harmony.MainActivity;

public class HarmonyService extends Service {
    private static final String LOG_TAG = HarmonyService.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "onStartCommand");

        String control = intent.getStringExtra(MainActivity.INTENT_CONTROL);
        switch (control)  {
            case MainActivity.INTENT_INITIATE_CALL:
                initiateCall();
                break;
            case MainActivity.INTENT_TERMINATE_CALL:
                terminateCall();
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }

    // this method may move to another class
    private void initiateCall() {
        Log.i(LOG_TAG, "initiateCall");
        showToastInService("initiateCall");
    }

    // this method may move to another class
    private void terminateCall() {
        Log.i(LOG_TAG, "terminateCall");
        showToastInService("terminateCall");
    }

    private void showToastInService(final String string) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
