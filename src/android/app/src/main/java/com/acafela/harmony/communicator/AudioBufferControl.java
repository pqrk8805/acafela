package com.acafela.harmony.communicator;

import android.util.Log;

import com.acafela.harmony.crypto.ICrypto;

import java.util.concurrent.ConcurrentLinkedQueue;

public class AudioBufferControl {
    private ConcurrentLinkedQueue<AudioData> IncommingpacketQueue;
    private static final String LOG_TAG = "AudioBufferControl";
    private int currentSeqNum;
    private ICrypto mCrypto;
    private int MAX_AUDIO_SEQNO;

    AudioBufferControl(ICrypto crypto,int maxBufferSize)
    {
        IncommingpacketQueue = new ConcurrentLinkedQueue<>();
        currentSeqNum = 0;
        mCrypto = crypto;
        MAX_AUDIO_SEQNO = maxBufferSize;
    }

    public void pushData(AudioData data)  {
        //int receiveSeqNum =  (recieveData[1]&0xFF)<<8 | (recieveData[2]&0xFF);
        //Log.i(LOG_TAG, "Packet received length: " + recieveData.length + " seqNo :" + receiveSeqNum);

        if((data.seqNo != currentSeqNum) && (data.seqNo != currentSeqNum +1) && (data.seqNo != 0 || currentSeqNum!=MAX_AUDIO_SEQNO) )//check loss data
        {
            Log.i(LOG_TAG, "Packet Loss  occured from" + currentSeqNum + " to :" + data.seqNo);
        }

        if(data.isPrimary==false && data.seqNo == currentSeqNum) // skip sub packet
            return;
        currentSeqNum = data.seqNo;
        IncommingpacketQueue.add(data);
    }
    public AudioData getData()  {
        AudioData oudData;
        oudData = IncommingpacketQueue.remove();
        return oudData;
    }
    public int size()
    {
        return IncommingpacketQueue.size();
    }

    public void clear()
    {
        IncommingpacketQueue.clear();
        IncommingpacketQueue = null;
    }
}
