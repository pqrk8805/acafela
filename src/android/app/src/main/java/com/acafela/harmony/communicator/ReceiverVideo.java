package com.acafela.harmony.communicator;

import android.util.Log;

import com.acafela.harmony.sip.SipMessage;

import java.net.DatagramSocket;

public class ReceiverVideo implements DataCommunicator {
    private static final int VIDEO_BUFFER_SIZE = 65507;
    private boolean UdpVoipReceiveVideoThreadRun = false;
    private DatagramSocket RecvVideoUdpSocket;
    private Thread UdpReceiveVideoThread = null;
    static final String LOG_TAG = "ReceiverVideo";
    private  String mIpAddress;
    private int mPort;

    public SipMessage.SessionType getType()
    {
        return SipMessage.SessionType.RECIEVEVIDEO;
    }
    public boolean setSession(String ip,int port)
    {
        this.mIpAddress = ip;
        this.mPort= port;
        return true;
    }

    public boolean startCommunicator()
    {
       // StartReceiveVideoThread();
        return true;
    }
    public boolean endCommunicator()
    {
        if (!UdpVoipReceiveVideoThreadRun) return false;
        if (UdpReceiveVideoThread != null && UdpReceiveVideoThread.isAlive()) {
            UdpVoipReceiveVideoThreadRun = false;
            RecvVideoUdpSocket.close();
            Log.i(LOG_TAG, "UdpReceiveDataThread Thread Join started");
            UdpVoipReceiveVideoThreadRun = false;
            try {
                UdpReceiveVideoThread.join();
            } catch (InterruptedException e) {
                Log.i(LOG_TAG, "UdpReceiveDataThread Join interruped");
            }
            Log.i(LOG_TAG, " UdpReceiveDataThread Join successs");
        }

        UdpReceiveVideoThread = null;
        RecvVideoUdpSocket = null;
        return true;
    }


/*
    private void StartReceiveVideoThread() {
        // Create thread for receiving audio data
        if ( UdpVoipReceiveVideoThreadRun) return;
        UdpVoipReceiveVideoThreadRun = true;
        UdpReceiveVideoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Create an instance of AudioTrack, used for playing back audio
                Log.i(LOG_TAG, "Receive Data Thread Started. Thread id: " + Thread.currentThread().getId());
                try {
                    // Setup socket to receive the audio data
                    RecvVideoUdpSocket = new DatagramSocket(null);
                    RecvVideoUdpSocket.setReuseAddress(true);
                    RecvVideoUdpSocket.bind(new InetSocketAddress(mPort));

                    while (UdpVoipReceiveVideoThreadRun) {
                        byte[] jpegbuf = new byte[VIDEO_BUFFER_SIZE];
                        DatagramPacket packet = new DatagramPacket(jpegbuf, VIDEO_BUFFER_SIZE);
                        RecvVideoUdpSocket.receive(packet);
                        if (packet.getLength() >0) {
                            final Bitmap bitmap = BitmapFactory.decodeByteArray(packet.getData(), 0, packet.getLength());
                            final Matrix mtx = new Matrix();
                            mtx.postRotate(-90);
                            final Bitmap rotator = Bitmap.createBitmap(bitmap, 0, 0,
                                    bitmap.getWidth(), bitmap.getHeight(), mtx,
                                    true);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ImageViewVideo.setImageBitmap(rotator);
                                }
                            });

                            //Log.i(LOG_TAG, "Video Packet received: " + packet.getLength());
                        } else
                            Log.i(LOG_TAG, "Invalid Packet LengthReceived: " + packet.getLength());

                    }
                    // close socket
                    RecvVideoUdpSocket.disconnect();
                    RecvVideoUdpSocket.close();
                } catch (SocketException e) {
                    UdpVoipReceiveVideoThreadRun = false;
                    Log.e(LOG_TAG, "SocketException: " + e.toString());
                } catch (IOException e) {
                    UdpVoipReceiveVideoThreadRun = false;
                    Log.e(LOG_TAG, "IOException: " + e.toString());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Clear Video Frame
                        ImageViewVideo.setImageBitmap(null);
                        Log.i(LOG_TAG, "Clear video Frame");
                    }
                });
            }

        });
        UdpReceiveVideoThread.start();

    }
    */
}
