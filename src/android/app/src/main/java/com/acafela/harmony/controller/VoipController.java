package com.acafela.harmony.controller;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class VoipController {
    public static final int CONTROL_DATA_PORT = 5123;
    public final static String UDP_VOIP_CTRL = "UdpVoIpControl";
    private static final String LOG_TAG = "UDPListenerService";
    private static final int BUFFER_SIZE = 128;
    private boolean UdpListenerThreadRun = false;
    private DatagramSocket socket;

    private void startListenerForUDP() {
        UdpListenerThreadRun = true;
        Thread UDPListenThread = new Thread(new Runnable() {
            public void run() {
                try {
                    // Setup the socket to receive incoming messages
                    byte[] buffer = new byte[BUFFER_SIZE];
                    socket = new DatagramSocket(null);
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress(CONTROL_DATA_PORT));
                    DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);
                    Log.i(LOG_TAG, "Incoming call listener started");
                    while (UdpListenerThreadRun) {
                        // Listen for incoming call requests
                        Log.i(LOG_TAG, "Listening for incoming calls");
                        socket.receive(packet);
                        String senderIP = packet.getAddress().getHostAddress();
                        String message = new String(buffer, 0, packet.getLength());
                        Log.i(LOG_TAG, "Got UDP message from " + senderIP + ", message: " + message);
                        //broadcastIntent(senderIP, message);
                    }
                    Log.e(LOG_TAG, "Call Listener ending");
                    socket.disconnect();
                    socket.close();

                } catch (Exception e) {
                    UdpListenerThreadRun = false;
                    Log.e(LOG_TAG, "no longer listening for UDP messages due to error " + e.getMessage());
                }
            }
        });
        UDPListenThread.start();
    }
}
