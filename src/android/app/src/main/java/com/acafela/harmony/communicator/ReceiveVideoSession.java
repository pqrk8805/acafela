package com.acafela.harmony.communicator;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.acafela.harmony.sip.SipMessage;

import java.net.InetAddress;

import static com.acafela.harmony.ui.AudioCallActivity.BROADCAST_RECEIVEVIDEO;
import static com.acafela.harmony.ui.AudioCallActivity.KEY_PORT;

public class ReceiveVideoSession implements DataCommunicator {
    static final String TAG = "ReceiveVideoSession";
    private boolean IsRunning = false;

    private InetAddress mIpAddress;
    private int mPort;
    private Context mContext;

    public ReceiveVideoSession(Context context) {
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
        sendBroadcast(mPort);
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
        return SipMessage.SessionType.RECIEVEVIDEO;
    }

    @Override
    public int getPortNum() {
        return mPort;
    }

    private void sendBroadcast(int port) {
        Log.i(TAG, "sendBroadcast BROADCAST_RECEIVEVIDEO");
        Log.i(TAG, "port: " + port);
        Intent intent = new Intent(BROADCAST_RECEIVEVIDEO);
        intent.putExtra(KEY_PORT, port);
        mContext.sendBroadcast(intent);
    }
}
