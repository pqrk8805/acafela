package com.acafela.harmony.communicator;


import java.io.IOException;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.media.audiofx.AcousticEchoCanceler; // AEC

import com.acafela.harmony.crypto.ICrypto;

import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;


public class SenderAudio implements DataSender {
    private  InetAddress mIpAddress;
    private int mPort;
    private Thread mSenderThread = null;
    private boolean senderAudioThreadRun = false;
    private ICrypto mCrypto;

    // AEC start
    private AcousticEchoCanceler mAudioEchoCanceler;
    private int mAudioRecordSessionId;
    // AEC end

    private static final int MILLISECONDS_IN_A_SECOND = 1000;
    private static final int SAMPLE_RATE = 8000; // Hertz
    private static final int SAMPLE_INTERVAL = 20;   // Milliseconds
    private static final int BYTES_PER_SAMPLE = 2;    // Bytes Per Sampl;e
    private static final int RAW_BUFFER_SIZE = SAMPLE_RATE / (MILLISECONDS_IN_A_SECOND / SAMPLE_INTERVAL) * BYTES_PER_SAMPLE;
    private static final int GSM_BUFFER_SIZE = 33;
    private static final String LOG_TAG = "SenderAudio";
    private boolean isSenderAudioRun=false;

    private static final boolean isTimeStamp = false;

    private int mSimVoice;


    public SenderAudio(ICrypto crypto)
    {
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

    public boolean startSender()
    {
        if(isSenderAudioRun)
            return false;
        senderAudioThreadRun = true;
        startSenderAudioThread();

        isSenderAudioRun = true;
        return true;
    }
    public boolean endSender()
    {
        if (mSenderThread != null && mSenderThread.isAlive()) {
            senderAudioThreadRun = false;
            Log.i(LOG_TAG, "mSenderThread Thread Join started");
            try {
                mSenderThread.join();
            } catch (InterruptedException e) {
                Log.i(LOG_TAG, "mSenderThread Join interruped");
            }
            Log.i(LOG_TAG, " mSenderThread Join successs");
        }
        senderAudioThreadRun = false;
        isSenderAudioRun = false;
        return true;
    }
    private InputStream OpenSimVoice(int SimVoice) {
        InputStream VoiceFile = null;
        switch (SimVoice) {
            case 0:
                break;
           /* case 1:
                VoiceFile = mContext.getResources().openRawResource(R.raw.t18k16bit);
                break;
            case 2:
                VoiceFile = mContext.getResources().openRawResource(R.raw.t28k16bit);
                break;
            case 3:
                VoiceFile = mContext.getResources().openRawResource(R.raw.t38k16bit);
                break;
            case 4:
                VoiceFile = mContext.getResources().openRawResource(R.raw.t48k16bit);
                break;*/
            default:
                break;
        }
        return VoiceFile;
    }

    private void startSenderAudioThread()
    {
        // Create thread for receiving audio data
        mSenderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Create an instance of AudioTrack, used for playing back audio
                Log.i(LOG_TAG, "Receive Data Thread Started. Thread id: " + Thread.currentThread().getId());
                InputStream InputPlayFile;

                AudioRecord Recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT));

                // AEC start
                mAudioRecordSessionId = Recorder.getAudioSessionId();

                if (AcousticEchoCanceler.isAvailable()) {
                    mAudioEchoCanceler = AcousticEchoCanceler.create(mAudioRecordSessionId);
                    Log.i("AEC", "audio echo canceler enable");

                    Log.i("AEC", "AEC is " + (mAudioEchoCanceler.getEnabled()?"enabled":"disabled"));

                    if ( !mAudioEchoCanceler.getEnabled() )
                    {
                        mAudioEchoCanceler.setEnabled(true);
                        Log.i("AEC", "AEC is " + (mAudioEchoCanceler.getEnabled()?"enabled":"disabled" +" after trying to disable"));
                    }
                }
                // AEC end

                int BytesRead;
                byte[] rawbuf = new byte[RAW_BUFFER_SIZE+5];
                //byte[] gsmbuf = new byte[GSM_BUFFER_SIZE];
                InputPlayFile = OpenSimVoice(mSimVoice);

                try
                {
                    DatagramSocket socket = new DatagramSocket();
                    Recorder.startRecording();
                    while (senderAudioThreadRun)
                    {
                        // Capture audio from microphone and send
                        if(isTimeStamp) {
                            Long tsLong = System.currentTimeMillis() % 10000;
                            rawbuf[3] = (byte) (tsLong & 0x000000ff);
                            rawbuf[2] = (byte) ((tsLong >> 8) & 0x000000ff);
                            rawbuf[1] = (byte) ((tsLong >> 16) & 0x000000ff);
                            rawbuf[0] = (byte) ((tsLong >> 24) & 0x000000ff);
                            //Log.i(LOG_TAG, "Packet send time: " + (tsLong));
                            BytesRead = Recorder.read(rawbuf, 5, RAW_BUFFER_SIZE);
                        }
                        else
                            BytesRead = Recorder.read(rawbuf, 0, RAW_BUFFER_SIZE);

                        if (InputPlayFile != null) {
                            BytesRead = InputPlayFile.read(rawbuf, 0, RAW_BUFFER_SIZE);
                            if (BytesRead != RAW_BUFFER_SIZE) {
                                InputPlayFile.close();
                                InputPlayFile = OpenSimVoice(mSimVoice);
                                BytesRead = InputPlayFile.read(rawbuf, 0, RAW_BUFFER_SIZE);
                            }
                        }
                        if (BytesRead == RAW_BUFFER_SIZE) {
                            //JniGsmEncodeB(rawbuf, gsmbuf);
                            //DatagramPacket packet = new DatagramPacket(gsmbuf, GSM_BUFFER_SIZE, RemoteIp, VOIP_DATA_UDP_PORT);
                            if(isTimeStamp) {
                                rawbuf[4] = (byte)0;//primary packet
                                DatagramPacket packet = new DatagramPacket(rawbuf, RAW_BUFFER_SIZE + 5, mIpAddress, mPort);
                                socket.send(packet);
                                rawbuf[4] = (byte)1;//sub packet
                                DatagramPacket packet2 = new DatagramPacket(rawbuf, RAW_BUFFER_SIZE + 5, mIpAddress, mPort);
                                socket.send(packet2);
                            } else {
                                byte[] encrypted = mCrypto.encrypt(rawbuf);
                                DatagramPacket packet = new DatagramPacket(
                                                            encrypted,
                                                            encrypted.length,
                                                            mIpAddress,
                                                            mPort);
                                socket.send(packet);
                            }
                        }
                    }
                    Recorder.stop();
                    Recorder.release();
                    if (InputPlayFile != null) InputPlayFile.close();
                    socket.disconnect();

                    // AEC start
                    mAudioEchoCanceler.release();
                    // AEC end

                } catch (SocketException e) {
                    senderAudioThreadRun = false;
                    Log.e(LOG_TAG, "SocketException: " + e.toString());
                } catch (IOException e) {
                    senderAudioThreadRun = false;
                    Log.e(LOG_TAG, "IOException: " + e.toString());
                }
            }
        });
        mSenderThread.start();
    }

}

