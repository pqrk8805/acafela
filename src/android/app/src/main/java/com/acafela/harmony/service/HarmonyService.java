package com.acafela.harmony.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.acafela.harmony.activity.CallActivity;
import com.acafela.harmony.communicator.ReceiverAudio;
import com.acafela.harmony.communicator.ReceiverVideo;
import com.acafela.harmony.communicator.SenderAudio;
import com.acafela.harmony.communicator.SenderVideo;
import com.acafela.harmony.crypto.Crypto;
import com.acafela.harmony.crypto.CryptoBroker;
import com.acafela.harmony.crypto.ICrypto;

public class HarmonyService extends Service {
    private static final String LOG_TAG = HarmonyService.class.getName();

    //temporary value;
    SenderAudio senderAudio;
    ReceiverAudio receiverAudio;
    SenderVideo senderVideo;
    ReceiverVideo receiverVideo;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "onCreate");

        Crypto.init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "onStartCommand");

        String control = intent.getStringExtra(CallActivity.INTENT_CONTROL);
        switch (control)  {
            case CallActivity.INTENT_INITIATE_CALL:
                String serverIp = intent.getStringExtra(CallActivity.INTENT_SERVERIP);
                initiateCall(serverIp);
                break;
            case CallActivity.INTENT_TERMINATE_CALL:
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
    private void initiateCall(String serverIp) {
        Log.i(LOG_TAG, "initiateCall");
        showToastInService("initiateCall");

        ICrypto crypto = CryptoBroker.getInstance().create("AES");
        crypto.init("12345".getBytes());

        int sendPort =5000;
        //int receivePort =5001;
        int receivePort =5000;
        //int sendPort =5002;
        //int receivePort =5003;

        senderAudio = new SenderAudio(crypto);
        senderAudio.setSession(serverIp,sendPort);
        senderAudio.startSender();

        receiverAudio = new ReceiverAudio(getApplicationContext(), crypto);
        receiverAudio.setSession(serverIp,receivePort);
        receiverAudio.startReceiver();
/*
        senderVideo = new SenderVideo();
        senderVideo.setSession(serverIp,6001);
        senderVideo.startSender();


        receiverVideo = new ReceiverVideo();
        receiverVideo.setSession(serverIp,6001);
        receiverVideo.startReceiver();*/
    }

    // this method may move to another class
    private void terminateCall() {
        senderAudio.endSender();
        receiverAudio.endReceiver();
        senderVideo.endSender();
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
