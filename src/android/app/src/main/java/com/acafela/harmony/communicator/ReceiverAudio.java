package com.acafela.harmony.communicator;

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

import com.acafela.harmony.crypto.ICrypto;
import com.acafela.harmony.sip.SipMessage;

import java.net.SocketException;
import com.acafela.harmony.sip.SipMessage;
import com.acafela.harmony.sip.SipMessage.SIPMessage;


public class ReceiverAudio implements DataCommunicator {
    private static final String LOG_TAG = "ReceiverAudio";
    private  InetAddress mIpAddress;
    private int mPort;
    private boolean UdpVoipReceiveDataThreadRun = false;
    private boolean  AudioIoPlayerThreadRun =false;
    private Thread mRecieverThread = null;
    private Thread mPlayerThread = null;
    private DatagramSocket RecvUdpSocket;
    private ConcurrentLinkedQueue<byte[]> IncommingpacketQueue;
    private boolean isReceverAudioRun=false;

    private Context mContext;
    private ICrypto mCrypto;

    public ReceiverAudio (Context context, ICrypto crypto)
    {
        mContext = context;
        mCrypto = crypto;
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
    public SipMessage.SessionType getType()
    {
        return SipMessage.SessionType.RECIEVEAUDIO;
    }
    public boolean setSession(InetAddress ip,int port)
    {
        this.mIpAddress = ip;
        this.mPort = port;
        return true;
    }

    public boolean startCommunicator()
    {
        if(isReceverAudioRun) {
            Log.i(LOG_TAG, "UdpReceiveDataThread Thread already Started started");
            return false;
        }
        IncommingpacketQueue = new ConcurrentLinkedQueue<>();

        UdpVoipReceiveDataThreadRun = true;
        AudioIoPlayerThreadRun = true;
        startReceiveDataThread();
        startAudioPlayerThread();

        isReceverAudioRun = true;
        return true;
    }
    public boolean endCommunicator()
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
            AudioIoPlayerThreadRun = false;
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
        AudioIoPlayerThreadRun = false;
        UdpVoipReceiveDataThreadRun = false;
        isReceverAudioRun = false;

        return true;
    }
    private void startReceiveDataThread()
    {
        // Create thread for receiving audio data
        mRecieverThread = new Thread(new Runnable() {
            int currentSeqNum;
            @Override
            public void run() {
                // Create an instance of AudioTrack, used for playing back audio
                Log.i(LOG_TAG, "Receive Data Thread Started. Thread id: " + Thread.currentThread().getId());
                try {
                    // Setup socket to receive the audio data
                    RecvUdpSocket = new DatagramSocket(null);
                    RecvUdpSocket.setReuseAddress(true);
                    RecvUdpSocket.bind(new InetSocketAddress(mPort));
                    //boolean prevPrimaryData =false;

                    while (UdpVoipReceiveDataThreadRun) {
                        if(isAudioHeader) {
                            byte[] recieveData = new byte[ (((RAW_BUFFER_SIZE) / 16 + 1) * 16) + AUDIO_HEADER_SIZE];
                            DatagramPacket packet = new DatagramPacket(recieveData, recieveData.length);

                            RecvUdpSocket.receive(packet);
                            int receiveSeqNum =  (recieveData[1]&0xFF)<<8 | (recieveData[2]&0xFF);
                            //Log.i(LOG_TAG, "Packet received length: " + recieveData.length + " seqNo :" + receiveSeqNum);

                            if((receiveSeqNum != currentSeqNum) && (receiveSeqNum != currentSeqNum +1) && (receiveSeqNum != 0 || currentSeqNum!=MAX_AUDIO_SEQNO) )//check loss data
                            {
                                Log.i(LOG_TAG, "Packet Loss  occured from" + currentSeqNum + " to :" + receiveSeqNum);
                            }

                            if(recieveData[0]!=0 && receiveSeqNum == currentSeqNum) // skip sub packet
                                continue;

                            currentSeqNum = receiveSeqNum;
                            byte[] plane = mCrypto.decrypt(recieveData, AUDIO_HEADER_SIZE, packet.getLength()- AUDIO_HEADER_SIZE);
                            if(plane!=null)
                                IncommingpacketQueue.add(plane);
                        }
                        else {

                            byte[] encrypted = new byte[(RAW_BUFFER_SIZE / 16 + 1) * 16];
                            DatagramPacket packet = new DatagramPacket(encrypted, encrypted.length);
                            RecvUdpSocket.receive(packet);

                            byte[] plane = mCrypto.decrypt(encrypted, 0, packet.getLength());
                            IncommingpacketQueue.add(plane);
                        }
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
                    while (AudioIoPlayerThreadRun)
                    {
                        if (IncommingpacketQueue.size() > 0) {
                            byte[] AudioOutputBufferBytes = IncommingpacketQueue.remove();
                            //if (!MainActivity.BoostAudio)
                            if (true) {
                                if(isAudioHeader)
                                    OutputTrack.write(AudioOutputBufferBytes, 0, RAW_BUFFER_SIZE);
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
