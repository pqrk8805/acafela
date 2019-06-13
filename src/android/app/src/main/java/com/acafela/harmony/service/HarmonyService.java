package com.acafela.harmony.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.acafela.harmony.ui.CallActivity;
import com.acafela.harmony.communicator.ReceiverAudio;
import com.acafela.harmony.communicator.ReceiverVideo;
import com.acafela.harmony.communicator.SenderAudio;
import com.acafela.harmony.communicator.SenderVideo;

import com.acafela.harmony.crypto.Crypto;
import com.acafela.harmony.crypto.CryptoBroker;
import com.acafela.harmony.crypto.ICrypto;

import com.acafela.harmony.controller.VoipController;

public class HarmonyService extends Service {
    private static final String LOG_TAG = HarmonyService.class.getName();


    //temporary value;
    VoipController controller;
    SenderAudio senderAudio;
    ReceiverAudio receiverAudio;
    SenderVideo senderVideo;
    ReceiverVideo receiverVideo;
    boolean isFirst=true;

    @Override
    public void onCreate() {
        super.onCreate();
        controller = new VoipController(getApplicationContext());
        controller.startListenerController();
        Log.i(LOG_TAG, "onCreate");

        Crypto.init();

        /* Just Test
        DirectoryServiceRpc directoryServiceRpc = new DirectoryServiceRpc(
                                            Config.SERVER_IP,
                                            Config.RPC_PORT_DIRECTORY_SERVICE);
        DirInfo info = DirInfo.newBuilder()
                              .setPhoneNumber("2964-6476")
                              .setAddress("10.0.1.3")
                              .build();
        Common.Error err = directoryServiceRpc.getBlockingStub().update(info);
        Log.i(LOG_TAG, "Error: " + err.getErr());
        directoryServiceRpc.shutdown();
        */
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "onStartCommand");

        String control = intent.getStringExtra(CallActivity.INTENT_CONTROL);
        switch (control)  {
            case CallActivity.INTENT_INITIATE_CALL:
                String serverIp = intent.getStringExtra(CallActivity.INTENT_SERVERIP);
                int serverSndPort = intent.getIntExtra(CallActivity.INTENT_SERVERSENDPORT, 0);
                int serverRcvPort = intent.getIntExtra(CallActivity.INTENT_SERVERRCVPORT, 0);
                initiateCall(serverIp, serverSndPort, serverRcvPort);
                break;
            case CallActivity.INTENT_TERMINATE_CALL:
                terminateCall();
                break;
            case CallActivity.INTENT_SIP_INVITE_CALL:

                sipinvite(intent.getStringExtra(CallActivity.INTENT_SERVERIP), intent.getIntExtra(CallActivity.INTENT_SERVERSENDPORT, 0), intent.getIntExtra(CallActivity.INTENT_SERVERRCVPORT, 0));

                break;
            case CallActivity.INTENT_SIP_ACCEPT_CALL:
                sipaccept();
                break;
            case CallActivity.INTENT_SIP_TERMINATE_CALL:
                sipterminate();
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
    private void initiateCall(String serverIp, int sendPort, int receivePort) {
        Log.i(LOG_TAG, "initiateCall");
        showToastInService("initiateCall");

        // ToDo:
        // 키를 서버에서 받아와야 함
        //
        byte[] tempKey = {-108, -110, -109, -7, -33, 126, 75, 78, 110, -25, -40, -109, -12, 40, -40, 96,};
        ICrypto crypto = CryptoBroker.getInstance().create("AES");
        crypto.init(tempKey);

        senderAudio = new SenderAudio(crypto);
        senderAudio.setSession(serverIp,sendPort);
        senderAudio.startCommunicator();

        receiverAudio = new ReceiverAudio(getApplicationContext(), crypto);
        receiverAudio.setSession(serverIp,receivePort);
        receiverAudio.startCommunicator();
    }


    // this method may move to another class
    private void terminateCall() {
        senderAudio.endCommunicator();
        receiverAudio.endCommunicator();

        controller.terminateCall();
        Log.i(LOG_TAG, "terminateCall");
        showToastInService("terminateCall");
    }

    private void sipinvite(String serverIp, int sendPort, int receivePort) {
        Log.i(LOG_TAG, "initiateCall");
        showToastInService("initiateCall");
        if(isFirst)
            isFirst =false;
        else
            controller.inviteCall(serverIp);
    }
    private void sipaccept() {
        controller.acceptCall();
        Log.i(LOG_TAG, "acceptCall");
        showToastInService("acceptCall");
    }
    private void sipterminate() {
        controller.terminateCall();
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
