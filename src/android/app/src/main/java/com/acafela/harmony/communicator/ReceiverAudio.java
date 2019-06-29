package com.acafela.harmony.communicator;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Process;
import android.util.Log;

import com.acafela.harmony.codec.audio.AudioCodecSync;
import com.acafela.harmony.codec.audio.AudioMediaFormat;
import com.acafela.harmony.crypto.ICrypto;
import com.acafela.harmony.sip.SipMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;

import javax.crypto.IllegalBlockSizeException;

import static com.acafela.harmony.codec.audio.AudioMediaFormat.AUDIO_SAMPLE_RATE;

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
    AudioMediaFormat mAudioMediaFormat = new AudioMediaFormat();
    AudioCodecSync mAudioDecoder = new AudioCodecSync(false);

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
        mAudioDecoder.start(mAudioMediaFormat.getAudioMediaFormat());

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
        mAudioDecoder.stop();

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
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                // Create an instance of AudioTrack, used for playing back audio
                Log.i(LOG_TAG, "Receive Data Thread Started. Thread id: " + Thread.currentThread().getId());
                try {
                    // Setup socket to receive the audio data
                    RecvUdpSocket = new DatagramSocket(null);
                    RecvUdpSocket.setReuseAddress(true);
                    RecvUdpSocket.bind(new InetSocketAddress(mPort));
                    int dataNum=0;

                    while (UdpVoipReceiveDataThreadRun) {
                        if(isAudioHeader) {
                            //byte[] recieveData = new byte[ (((RAW_BUFFER_SIZE) / 16 + 1) * 16) + AUDIO_HEADER_SIZE];
                            byte[] recieveData = new byte[51];
                            DatagramPacket packet = new DatagramPacket(recieveData, recieveData.length);

                            RecvUdpSocket.receive(packet);

                            AudioData data = new AudioData();
                            data.seqNo = (recieveData[1]&0xFF)<<8 | (recieveData[2]&0xFF);
                            if(recieveData[0] == 0)
                                data.isPrimary = true;
                           //Log.i(LOG_TAG, " data" + data.seqNo + " lenth : " + recieveData.length);

                            if(mAudioControl.isValidCheck(data.seqNo)) {
                                if(dataNum+1 != data.seqNo) {
                                    Log.e(LOG_TAG, "skip data" + (dataNum + 1));
                                }
                                dataNum = data.seqNo;
                                //Log.e(LOG_TAG, " data" + data.seqNo);
                                byte[] encryptBuf = Arrays.copyOfRange(recieveData, AUDIO_HEADER_SIZE, packet.getLength());
                                if(encryptBuf == null) continue;

                                byte[] decryptBuf;
                                try {
                                    decryptBuf = mCrypto.decrypt(encryptBuf, 0, encryptBuf.length);
                                } catch(IllegalArgumentException e){
                                    e.printStackTrace();
                                    continue;
                                } /*catch(IllegalBlockSizeException h){
                                    //.printStackTrace();
                                    continue;
                                }*/

                                if(decryptBuf == null) continue;
                                data.data = decryptBuf;
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
                                .setSampleRate(AUDIO_SAMPLE_RATE)
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

                        //data.data = decodedBuf;


                        if(audioData!=null) {
                            byte[] decodedBuf;

                            try {
                                decodedBuf = mAudioDecoder.handle(audioData.data);
                            } catch(IllegalArgumentException e){
                                e.printStackTrace();
                                continue;
                            }
                            if(decodedBuf==null)  continue;
                            //if (!MainActivity.BoostAudio)
                            if (true) {
                                if(isAudioHeader) {
                                    OutputTrack.write(decodedBuf, 0, RAW_BUFFER_SIZE);
                                } else
                                    OutputTrack.write(decodedBuf, 0, RAW_BUFFER_SIZE);
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
