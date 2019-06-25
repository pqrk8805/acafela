package com.acafela.harmony.communicator;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class VideoHandler {
    private static final String TAG = VideoHandler.class.getName();

    HandlerThread mHandlerThread;
    Handler mHandler;

    DatagramSocket mSocket;
    private InetAddress mIpAddress;
    int mPort;

    public void start(String ip, int port) {
        mHandlerThread = new HandlerThread("VideoHandler");
        mHandlerThread.start();
        Looper looper = mHandlerThread.getLooper();
        mHandler = new Handler(looper);

        mPort = port;
        try {
            mIpAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            mSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        mHandlerThread.quit();
        mHandler = null;
    }

    public void sendFrame(final byte[] byteArray) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                DatagramPacket packet = new DatagramPacket(
                        byteArray,
                        byteArray.length,
                        mIpAddress,
                        mPort);
                try {
                    mSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
