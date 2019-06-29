package com.acafela.harmony.communicator;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.util.Log;

import com.acafela.harmony.codec.audio.AudioCodecSync;
import com.acafela.harmony.codec.audio.AudioMediaFormat;
import com.acafela.harmony.crypto.ICrypto;
import com.acafela.harmony.sip.SipMessage;
import com.acafela.harmony.util.AverageTimeCheck;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static com.acafela.harmony.codec.audio.AudioMediaFormat.AUDIO_SAMPLE_RATE;
import static com.acafela.harmony.codec.audio.AudioMediaFormat.RAW_BUFFER_SIZE;


public class SenderAudio implements DataCommunicator {
    private  InetAddress mIpAddress;
    private int mPort;
    private Thread mSenderThread = null;
    private boolean senderAudioThreadRun = false;
    private static final String LOG_TAG = "SenderAudio";
    private boolean isSenderAudioRun=false;
    private ICrypto mCrypto;
    // AEC start
    private AcousticEchoCanceler mAudioEchoCanceler;
    private int mAudioRecordSessionId;
    // AEC end
    AudioMediaFormat mAudioMediaFormat = new AudioMediaFormat();
    AudioCodecSync mAudioEncoder = new AudioCodecSync(true);
    private static int mPacketSeq= 0;

    private AverageTimeCheck encodeTimeCheck = new AverageTimeCheck();
    private AverageTimeCheck encrytionTimeCheck = new AverageTimeCheck();
    private AverageTimeCheck socketSendTimeCheck = new AverageTimeCheck();

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
    public boolean setSession(InetAddress ip,int port)
    {
        this.mIpAddress = ip;
        this.mPort = port;
        return true;
    }
    public int getPortNum()
    {
        return mPort;
    }
    public SipMessage.SessionType getType()
    {
        return SipMessage.SessionType.SENDAUDIO;
    }
    public boolean startCommunicator()
    {
        if(isSenderAudioRun)
            return false;
        mAudioEncoder.start(mAudioMediaFormat.getAudioMediaFormat());
        senderAudioThreadRun = true;
        startSenderAudioThread();

        isSenderAudioRun = true;

        encodeTimeCheck.init("encodeTimeCheck");
        encrytionTimeCheck.init("encrytionTimeCheck");
        socketSendTimeCheck.init("socketSendTimeCheck");
        return true;
    }
    public boolean endCommunicator()
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
        mAudioEncoder.stop();
        senderAudioThreadRun = false;
        isSenderAudioRun = false;

        encodeTimeCheck.finish();
        encrytionTimeCheck.finish();
        socketSendTimeCheck.finish();
        return true;
    }

    private void startSenderAudioThread()
    {
        mPacketSeq = 0;

        // Create thread for receiving audio data
        mSenderThread = new Thread(new Runnable() {
            @Override
            public void run() {

                // Create an instance of AudioTrack, used for playing back audio
                Log.i(LOG_TAG, "Receive Data Thread Started. Thread id: " + Thread.currentThread().getId());
                InputStream InputPlayFile;
                byte[] SendBuffer = new byte[AUDIO_HEADER_SIZE + RAW_BUFFER_SIZE + 16];

                AudioRecord Recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT));

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
                byte[] rawbuf = new byte[RAW_BUFFER_SIZE];

                try
                {
                    DatagramSocket socket = new DatagramSocket();
                    Recorder.startRecording();
                    while (senderAudioThreadRun)
                    {
                        // Capture audio from microphone and send
                        BytesRead = Recorder.read(rawbuf, 0, RAW_BUFFER_SIZE);

                        if (BytesRead == RAW_BUFFER_SIZE) {
                            if(isAudioHeader) {
                                SendBuffer[2] = (byte) (mPacketSeq & 0x000000ff);
                                SendBuffer[1] = (byte) ((mPacketSeq>> 8) & 0x000000ff);
                                SendBuffer[0] = (byte) 0;//primary packet
                                if(mPacketSeq++ == MAX_AUDIO_SEQNO) mPacketSeq=0;
                                encodeTimeCheck.timeCheckStart();
                                byte[] encodedBuf = mAudioEncoder.handle(rawbuf);
                                encodeTimeCheck.timeCheckFinish();
                                if(encodedBuf == null)
                                    continue;
                                //Log.e(LOG_TAG,"encode size"+ encodedBuf.length);
                                encrytionTimeCheck.timeCheckStart();
                                byte[] encrypted= mCrypto.encrypt(encodedBuf, 0, encodedBuf.length);
                                encrytionTimeCheck.timeCheckFinish();
                                System.arraycopy(encrypted,0,SendBuffer,AUDIO_HEADER_SIZE,encrypted.length);
                                //Log.e(LOG_TAG,"encrypted size"+ encrypted.length);
                                int size = encrypted.length + AUDIO_HEADER_SIZE;
                                //Log.i(LOG_TAG, "Packet send length: " +  size );

                                DatagramPacket packet = new DatagramPacket(
                                        SendBuffer,
                                       // SendBuffer.length,
                                        size,
                                        mIpAddress,
                                        mPort);
                                socketSendTimeCheck.timeCheckStart();
                                socket.send(packet);
                                socketSendTimeCheck.timeCheckFinish();

                                SendBuffer[0]=1;
                                DatagramPacket subPacket = new DatagramPacket(
                                        SendBuffer,
                                        //SendBuffer.length,
                                        size,
                                        mIpAddress,
                                        mPort);
                                socket.send(subPacket);
                            } else {
                                byte[] encrypted = mCrypto.encrypt(rawbuf, 0, RAW_BUFFER_SIZE);
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

