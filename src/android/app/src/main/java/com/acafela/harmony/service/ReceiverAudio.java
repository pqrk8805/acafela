package com.acafela.harmony.service;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.net.InetSocketAddress;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Process;
import android.util.Log;
import java.net.SocketException;

public class ReceiverAudio implements DataReceiver {
    private static final int MILLISECONDS_IN_A_SECOND = 1000;
    private static final int SAMPLE_RATE = 8000; // Hertz
    private static final int SAMPLE_INTERVAL = 20;   // Milliseconds
    private static final int BYTES_PER_SAMPLE = 2;    // Bytes Per Sampl;e
    private static final int RAW_BUFFER_SIZE = SAMPLE_RATE / (MILLISECONDS_IN_A_SECOND / SAMPLE_INTERVAL) * BYTES_PER_SAMPLE;
    private static final int GSM_BUFFER_SIZE = 33;
    private static final boolean isTimeStamp = true;

    private static final String LOG_TAG = "ReceiverAudio";
    private  InetAddress mIpAddress;
    private int mPort;
    private boolean UdpVoipReceiveDataThreadRun = false;
    private boolean  AudioIoThreadThreadRun =false;
    private Thread mRecieverThread = null;
    private Thread mPlayerThread = null;
    private DatagramSocket RecvUdpSocket;
    private ConcurrentLinkedQueue<byte[]> IncommingpacketQueue;

    private Context mContext;

    ReceiverAudio (Context context) {
        mContext = context;
    }
    public boolean setSession(String ip,int port)
    {
        try {
            this.mIpAddress = InetAddress.getByName(ip);
            //this.mIpAddress = ip;
            this.mPort = port;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception Answer Messagwe: " + e);
            return false;
        }
        return true;
    }

    public boolean startReceiver()
    {
        IncommingpacketQueue = new ConcurrentLinkedQueue<>();
        startReceiveDataThread();
        startAudioPlayerThread();
        UdpVoipReceiveDataThreadRun = true;
        AudioIoThreadThreadRun = true;
        return true;
    }
    public boolean endReceiver()
    {
        if (mRecieverThread != null && mRecieverThread.isAlive()) {
            UdpVoipReceiveDataThreadRun = false;
            RecvUdpSocket.close();
            Log.i(LOG_TAG, "UdpReceiveDataThread Thread Join started");
            try {
                mRecieverThread.join();
            } catch (InterruptedException e) {
                Log.i(LOG_TAG, "UdpReceiveDataThread Join interruped");
            }
            Log.i(LOG_TAG, " UdpReceiveDataThread Join successs");
        }
        if (mPlayerThread != null && mPlayerThread.isAlive()) {
            AudioIoThreadThreadRun = false;
            Log.i(LOG_TAG, "Audio Thread Join started");

            try {
                mPlayerThread.join();
            } catch (InterruptedException e) {
                Log.i(LOG_TAG, "Audio Thread Join interruped");
            }
            Log.i(LOG_TAG, "Audio Thread Join successs");
        }

        mPlayerThread = null;
        mRecieverThread = null;
        IncommingpacketQueue = null;
        RecvUdpSocket = null;

        UdpVoipReceiveDataThreadRun = false;
        return true;
    }
    private void startReceiveDataThread()
    {
        // Create thread for receiving audio data
        mRecieverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Create an instance of AudioTrack, used for playing back audio
                Log.i(LOG_TAG, "Receive Data Thread Started. Thread id: " + Thread.currentThread().getId());
                try {
                    // Setup socket to receive the audio data
                    RecvUdpSocket = new DatagramSocket(null);
                    RecvUdpSocket.setReuseAddress(true);
                    RecvUdpSocket.bind(new InetSocketAddress(mPort));

                    while (UdpVoipReceiveDataThreadRun) {
                        if(isTimeStamp) {
                            byte[] rawbuf = new byte[RAW_BUFFER_SIZE+4];
                            DatagramPacket packet = new DatagramPacket(rawbuf, RAW_BUFFER_SIZE+4);

                            RecvUdpSocket.receive(packet);
                            Long tsLong = System.currentTimeMillis() % 10000;

                            int timestamp;
                            timestamp =  (rawbuf[0]&0xFF)<<24;
                            timestamp += (rawbuf[1]&0xFF)<<16;
                            timestamp += (rawbuf[2]&0xFF)<<8;
                            timestamp += rawbuf[3]&0xFF;
                            //Log.i(LOG_TAG, "Packet received: " + timestamp);
                            Log.i(LOG_TAG, "Packet latency: " + (tsLong - timestamp));
                            //Log.i(LOG_TAG, "Packet received: " + packet.getLength());
                            IncommingpacketQueue.add(rawbuf);
                        }
                        else {
                            byte[] rawbuf = new byte[RAW_BUFFER_SIZE];
                            DatagramPacket packet = new DatagramPacket(rawbuf, RAW_BUFFER_SIZE);
                            //Log.i(LOG_TAG, "Packet received: " + packet.getLength());
                            RecvUdpSocket.receive(packet);
                            IncommingpacketQueue.add(rawbuf);
                        }
                        /*byte[] rawbuf = new byte[RAW_BUFFER_SIZE];
                        byte[] gsmbuf = new byte[GSM_BUFFER_SIZE];
                        DatagramPacket packet = new DatagramPacket(gsmbuf, GSM_BUFFER_SIZE);
                        Log.i(LOG_TAG, "Packet received: " + packet.getLength());
                        RecvUdpSocket.receive(packet);
                        if (packet.getLength() == GSM_BUFFER_SIZE) {
                            JniGsmDecodeB(packet.getData(), rawbuf);
                            IncommingpacketQueue.add(rawbuf);
                            //Log.i(LOG_TAG, "Packet received: " + packet.getLength());
                        } else
                            Log.i(LOG_TAG, "Invalid Packet LengthReceived: " + packet.getLength());*/

                    }
                    // close socket
                    RecvUdpSocket.disconnect();
                    RecvUdpSocket.close();
                } catch (SocketException e) {
                    UdpVoipReceiveDataThreadRun = false;
                    Log.e(LOG_TAG, "SocketException: " + e.toString());
                } catch (IOException e) {
                    UdpVoipReceiveDataThreadRun = false;
                    Log.e(LOG_TAG, "IOException: " + e.toString());
                }
            }
        });
        mRecieverThread.start();
    }

    private void startAudioPlayerThread()
    {
        // Creates the thread for capturing and transmitting audio
        AudioIoThreadThreadRun = true;
        mPlayerThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                int PreviousAudioManagerMode = 0;
                if (audioManager != null) {
                    PreviousAudioManagerMode = audioManager.getMode();
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION); //Enable AEC
                }

                // Create an instance of the AudioRecord class
                Log.i(LOG_TAG, "Audio Thread started. Thread id: " + Thread.currentThread().getId());

                AudioTrack OutputTrack = new AudioTrack.Builder()
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                //	.setFlags(AudioAttributes.FLAG_LOW_LATENCY) //This is Nougat+ only (API 25) comment if you have lower
                                .build())
                        .setAudioFormat(new AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(SAMPLE_RATE)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                        .setBufferSizeInBytes(RAW_BUFFER_SIZE)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        //.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY) //Not until Api 26
                        //.setSessionId(Recorder.getAudioSessionId())
                        .build();
                {
                    OutputTrack.play();
                    while (AudioIoThreadThreadRun)
                    {
                        if (IncommingpacketQueue.size() > 0) {
                            byte[] AudioOutputBufferBytes = IncommingpacketQueue.remove();
                            //if (!MainActivity.BoostAudio)
                            if (true) {
                                if(isTimeStamp)
                                    OutputTrack.write(AudioOutputBufferBytes, 4, RAW_BUFFER_SIZE);
                                else
                                    OutputTrack.write(AudioOutputBufferBytes, 0, RAW_BUFFER_SIZE);
                            } else {
                                short[] AudioOutputBufferShorts = new short[AudioOutputBufferBytes.length / 2];
                                // to turn bytes to shorts as either big endian or little endian.
                                ByteBuffer.wrap(AudioOutputBufferBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(AudioOutputBufferShorts);
                                for (int i = 0; i < AudioOutputBufferShorts.length; i++) { // 16bit sample size
                                    int value = AudioOutputBufferShorts[i] * 10; //increase level by gain=20dB: Math.pow(10., dB/20.);  dB to gain factor
                                    if (value > 32767) {
                                        value = 32767;
                                    } else if (value < -32767) {
                                        value = -32767;
                                    }
                                    AudioOutputBufferShorts[i] = (short) value;
                                }
                                // to turn shorts back to bytes.
                                //byte[] AudioOutputBufferBytes2 = new byte[AudioOutputBufferShorts.length * 2];
                                //ByteBuffer.wrap(AudioOutputBufferBytes2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(AudioOutputBufferShorts);
                                //OutputTrack.write(AudioOutputBufferBytes2, 0, RAW_BUFFER_SIZE);
                                OutputTrack.write(AudioOutputBufferShorts, 0, AudioOutputBufferShorts.length);
                            }
                        }
                    }
                    // Stop Audio Thread);
                    OutputTrack.stop();
                    OutputTrack.flush();
                    OutputTrack.release();


                    if (audioManager != null) audioManager.setMode(PreviousAudioManagerMode);
                    Log.i(LOG_TAG, "Audio Thread Stopped");
                }
            }
        });
        mPlayerThread.start();
    }

}
