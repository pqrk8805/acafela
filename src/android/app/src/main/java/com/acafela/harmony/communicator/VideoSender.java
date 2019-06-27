package com.acafela.harmony.communicator;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class VideoSender {
    private static final String TAG = VideoSender.class.getName();

    HandlerThread mHandlerThread;
    Handler mHandler;
    boolean mIsStarted = false;

    DatagramSocket mSocket;
    private InetAddress mIpAddress;
    int mPort;

    public void start(final String ip, final int port) {
        Log.i(TAG, "start");
        mIsStarted = false;
        mHandlerThread = new HandlerThread("VideoSender");
        mHandlerThread.start();
        Looper looper = mHandlerThread.getLooper();
        mHandler = new Handler(looper);

        mPort = port;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
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
                mIsStarted = true;
                Log.i(TAG, "start completed, " + "ip: " + mIpAddress.getHostAddress() + ", port: " + port);
            }
        });
    }

    public void stop() {
        mIsStarted = false;
        mHandlerThread.quit();
        mHandler = null;
    }

    public void sendFrame(final byte[] byteArray) {
        if (!mIsStarted) {
            return;
        }
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
