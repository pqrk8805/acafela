package com.acafela.harmony.communicator;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.acafela.harmony.sip.SipMessage;

import java.net.InetAddress;

import static com.acafela.harmony.ui.AudioCallActivity.BROADCAST_SENDVIDEO;
import static com.acafela.harmony.ui.AudioCallActivity.KEY_IP;
import static com.acafela.harmony.ui.AudioCallActivity.KEY_PORT;

public class SendVideoSession implements DataCommunicator {
    private static final String TAG = "SendVideoSession";

    private boolean IsRunning = false;

    private InetAddress mIpAddress;
    private int mPort;
    private Context mContext;

    public SendVideoSession(Context context) {
        mContext = context;
    }

    @Override
    public boolean setSession(String ip,int port)
    {
        try {
            this.mIpAddress = InetAddress.getByName(ip);
            this.mPort = port;
        } catch (Exception e) {
            Log.e(TAG, "Exception Answer Message: " + e);
            return false;
        }
        return true;
    }

    @Override
    public boolean startCommunicator()
    {
        Log.i(TAG, "startCommunicator");
        if (IsRunning) {
            return false;
        }
        IsRunning = true;
        sendBroadcast(mIpAddress.getHostAddress(), mPort);
        return true;
    }

    @Override
    public boolean endCommunicator()
    {
        Log.i(TAG, "endCommunicator");
        if (!IsRunning) {
            return false;
        }
        IsRunning = false;
        return true;
    }

    @Override
    public SipMessage.SessionType getType() {
        return SipMessage.SessionType.SENDVIDEO;
    }

    @Override
    public int getPortNum() {
        return mPort;
    }

    private void sendBroadcast(String ip,int port) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "sendBroadcast BROADCAST_SENDVIDEO");
        Log.i(TAG, "ip: " + ip + ", port: " + port);
        Intent intent = new Intent(BROADCAST_SENDVIDEO);
        intent.putExtra(KEY_IP, ip);
        intent.putExtra(KEY_PORT, port);
        mContext.sendBroadcast(intent);
    }
}

