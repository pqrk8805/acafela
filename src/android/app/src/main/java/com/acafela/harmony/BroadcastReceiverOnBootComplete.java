package com.acafela.harmony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.acafela.harmony.service.harmonyService;

public class BroadcastReceiverOnBootComplete extends BroadcastReceiver {

    static final String LOG_TAG = "VoipDemo-BROB";

    @Override
    public void onReceive(Context context, Intent intent) {
        String ActionString = intent.getAction();
        if (ActionString != null) {
            if (ActionString.equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                // Start UDP listen Service
                Intent serviceIntent = new Intent(context, harmonyService.class);
                    context.startService(serviceIntent);
                    Log.e(LOG_TAG, "Started UDPListenerService.class");

            }
        }
    }
}
