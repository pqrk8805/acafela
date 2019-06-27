package com.acafela.harmony.communicator;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

public class VideoSenderThread extends Thread {
    private static final String TAG = VideoSenderThread.class.getName();

    private boolean mIsRunning;

    private DatagramSocket mSocket;
    private InetAddress mIpAddress;
    int mPort;
    private static LinkedBlockingQueue<byte[]> mFrameQueue = new LinkedBlockingQueue<>(100);

    public void setAddress(String ip, int port) {
        Log.i(TAG, "setAddress");
        try {
            mIpAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        mPort = port;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    private boolean initSocket() {
        try {
            if (mSocket == null) {
                mSocket = new DatagramSocket(mPort);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        Log.i(TAG, "initSocket Completed");
        return true;
    }

    @Override
    public void run() {
        Log.i(TAG, "Start, ip: " + mIpAddress.getHostAddress() + ", port: " + mPort);
        super.run();

        mIsRunning = true;
        if (!initSocket()) {
            return;
        }

        while(mIsRunning) {
            byte[] frame = null;
            try {
//                Log.i(TAG, "before take");
                    frame = mFrameQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
//            Log.i(TAG, "mFrameQueue is taken: " + frame.length);
            DatagramPacket packet = new DatagramPacket(
                    frame,
                    frame.length,
                    mIpAddress,
                    mPort);
            try {
                if (mSocket != null) {
                    mSocket.send(packet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            Log.i(TAG, "Send Video Packet: " + frame.length);
        }

        kill();
    }

    public void kill() {
        Log.i(TAG, "kill");
        mIsRunning = false;
        this.interrupt();
        mFrameQueue.clear();
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }
    }

    public void enqueueFrame(byte[] frame) {
        if (!mIsRunning) {
            return;
        }
        try {
            boolean success = mFrameQueue.offer(frame);
            if (!success) {
                Log.i(TAG, "Offer to Queue is failed");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
//        Log.i(TAG, "enqueueFrame mFrameQueue.size(): " + mFrameQueue.size());
//        Log.i(TAG, "enqueueFrame: " + frame.length);
    }
}
