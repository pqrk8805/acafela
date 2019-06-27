package com.acafela.harmony.communicator;

import android.util.Log;

import com.acafela.harmony.codec.video.IVideoDecoder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class VideoReceiverThread extends Thread {
    private static final String TAG = VideoReceiverThread.class.getName();

    private static final int MAX_BUFFER_SIZE = 65000;

    private boolean mIsRunning;
    private DatagramSocket mSocket;
    private IVideoDecoder mVideoDecoder;
    private int mPort;

    public void setDecoder(IVideoDecoder videoDecoder, int port) {
        Log.i(TAG, "setDecoder");
        mVideoDecoder = videoDecoder;
        mPort = port;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    private boolean initSocket() {
        try {
            if (mSocket == null) {
                mSocket = new DatagramSocket(mPort);
                Log.i(TAG, "initSocket Completed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public void run() {
        Log.i(TAG, "Start, port: " + mPort);
        super.run();

        mIsRunning = true;
        byte[] byteArray = new byte[MAX_BUFFER_SIZE];
        if (!initSocket()) {
            return;
        }
        DatagramPacket packet = new DatagramPacket(byteArray, MAX_BUFFER_SIZE);
        while(mIsRunning) {
            try {
                mSocket.receive(packet);
                byte[] receivedData = new byte[packet.getLength()];
                System.arraycopy(
                        packet.getData(),
                        packet.getOffset(),
                        receivedData,
                        0,
                        packet.getLength());
                mVideoDecoder.enqueueInputBytes(receivedData);
//                Log.i(TAG, "Receive Video Packet: " + receivedData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        kill();
    }

    public void kill() {
        Log.i(TAG, "kill");
        this.interrupt();
        mIsRunning = false;
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }
    }
}
