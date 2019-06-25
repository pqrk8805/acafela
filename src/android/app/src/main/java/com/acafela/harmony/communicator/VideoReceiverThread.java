package com.acafela.harmony.communicator;

import com.acafela.harmony.codec.video.IVideoDecoder;
import com.acafela.harmony.codec.video.VideoDecodeAsyncSurface;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class VideoReceiverThread extends Thread {

    private static final int MAX_BUFFER_SIZE = 65000;

    private boolean mIsRunning;
    private DatagramSocket mSocket;
    private IVideoDecoder mVideoDecoder;
    private int mPort;

    public VideoReceiverThread(IVideoDecoder videoDecoder, int port) {
        mVideoDecoder = videoDecoder;
        mPort = port;
    }

    @Override
    public void run() {
        super.run();

        mIsRunning = true;
        byte[] byteArray = new byte[MAX_BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(byteArray, MAX_BUFFER_SIZE);
        try {
            mSocket = new DatagramSocket(mPort);
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }

        while(mIsRunning) {
            try {
                mSocket.receive(packet);
                byte[] receivedData = new byte[packet.getLength()];
                System.arraycopy(
                        packet.getLength(),
                        packet.getOffset(),
                        receivedData,
                        0,
                        packet.getLength());
                mVideoDecoder.enqueueInputBytes(receivedData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        kill();
    }

    public void kill() {
        mIsRunning = false;
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }
    }
}
