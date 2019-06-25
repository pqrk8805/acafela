package com.acafela.harmony.communicator;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
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

public class ReceiverAudio implements DataCommunicator {
    private static final String LOG_TAG = "ReceiverAudio";
    private  InetAddress mIpAddress;
    private int mPort;
    private boolean UdpVoipReceiveDataThreadRun = false;
    private boolean  AudioIoPlayerThreadRun =false;
    private Thread mRecieverThread = null;
    private Thread mPlayerThread = null;
    private DatagramSocket RecvUdpSocket;

    private boolean isReceverAudioRun=false;

    private Context mContext;
    private ICrypto mCrypto;
    private AudioBufferControl mAudioControl;

    public ReceiverAudio (Context context, ICrypto crypto)
    {
        mContext = context;
        mCrypto = crypto;
        mAudioControl = new AudioBufferControl(mCrypto,MAX_AUDIO_SEQNO);
    }
    public int getPortNum()
    {
        return mPort;
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

        mAudioControl.clear();
        mPlayerThread = null;
        mRecieverThread = null;
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
                        if(isAudioHeader) {
                            byte[] recieveData = new byte[ (((RAW_BUFFER_SIZE) / 16 + 1) * 16) + AUDIO_HEADER_SIZE];
                            DatagramPacket packet = new DatagramPacket(recieveData, recieveData.length);

                            RecvUdpSocket.receive(packet);
                            {
                                AudioData data = new AudioData();
                                data.seqNo = (recieveData[1]&0xFF)<<8 | (recieveData[2]&0xFF);
                                if(recieveData[0] == 0)
                                    data.isPrimary = true;
                                data.length = packet.getLength() - AUDIO_HEADER_SIZE;
                                data.data = Arrays.copyOfRange(recieveData, AUDIO_HEADER_SIZE, packet.getLength());
                                //Log.i(LOG_TAG, "Packet received length: " + recieveData.length + " seqNo :" + data.length);
                                mAudioControl.pushData(data);
                            }
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
                        //if(mAudioControl.size()>0) {
                        AudioData audioData = mAudioControl.getData();
                        if(audioData!=null) {
                            //byte[] outStream = mCrypto.decrypt(audioData.data, 0, audioData.data.length);

                            //if (!MainActivity.BoostAudio)
                            if (true) {
                                if(isAudioHeader)
                                    OutputTrack.write(audioData.data, 0, RAW_BUFFER_SIZE);
                                else
                                    OutputTrack.write(audioData.data, 0, RAW_BUFFER_SIZE);
                            } else {
                                short[] AudioOutputBufferShorts = new short[audioData.data.length / 2];
                                // to turn bytes to shorts as either big endian or little endian.
                                for (int i = 0; i < AudioOutputBufferShorts.length; i++) { // 16bit sample size
                                    int value = AudioOutputBufferShorts[i] * 10; //increase level by gain=20dB: Math.pow(10., dB/20.);  dB to gain factor
                                    if (value > 32767) {
                                        value = 32767;
                                    } else if (value < -32767) {
                                        value = -32767;
                                    }
                                    AudioOutputBufferShorts[i] = (short) value;
                                }
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
