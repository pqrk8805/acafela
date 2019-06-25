package com.acafela.harmony.communicator;

import android.util.Log;

import com.acafela.harmony.crypto.ICrypto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

public class AudioBufferControl {
    private static final String LOG_TAG = "AudioBufferControl";
    private int MAX_DISTANCE=10;
    private int playerSeqNum;
    private int receiveSeqNum;
    private ICrypto mCrypto;
    private int MAX_AUDIO_SEQNO;
    private ArrayList mAudioDataQueue;
    private boolean isFirstFeeding;
    static Semaphore mSemaphore;

    AudioBufferControl(ICrypto crypto,int maxBufferSize)
    {
        mAudioDataQueue = new ArrayList<AudioData>();
        playerSeqNum = 0;
        mCrypto = crypto;
        MAX_AUDIO_SEQNO = maxBufferSize;
        isFirstFeeding = true;
        mSemaphore= new Semaphore(1);
        mCrypto = crypto;
    }

    public void pushData(AudioData data)  {
        try {
            mSemaphore.acquire();
            if (isValidCheck(data.seqNo, MAX_DISTANCE))
            {
                data.data = mCrypto.decrypt(data.data, 0, data.length);
                data.length = data.data.length;
            }
            mAudioDataQueue.add(data);
            receiveSeqNum = data.seqNo;
            mSemaphore.release();
        } catch (InterruptedException e) {

            e.printStackTrace();

        }
    }
    public AudioData getData()  {
        AudioData outData = null;
        try {
            mSemaphore.acquire();
            if (isFirstFeeding) {
                if (mAudioDataQueue.size() == MAX_DISTANCE / 2) {
                    playerSeqNum = getFirstPacketNo();
                    isFirstFeeding = false;
                } else {
                    mSemaphore.release();
                    return null;
                }
            }

            //Exception : recieve seqNo차이가 너무큰 경우 초기화
            if (checkDistanceError()) {
                playerSeqNum = getFirstPacketNo();
            }

            //현재 seqnumber 재생
            outData = getDataBySeqNo(playerSeqNum);
            if(outData!=null) {
                    //Log.e(LOG_TAG, "currSeq " + outData.seqNo);
            }

            //현재 seq 없는경우 이전거 재생
            if(outData == null) {
                int distance;
                for(distance = 1; distance < MAX_DISTANCE; distance++) {
                    int tempSeq = playerSeqNum - distance;
                    if(tempSeq < 0)
                        tempSeq = MAX_AUDIO_SEQNO - (distance - playerSeqNum);

                    outData = getDataBySeqNo(tempSeq);
                    if(outData!=null)
                    {
                        Log.e(LOG_TAG, "loss packet" + playerSeqNum + "recovery packet " + outData.seqNo);
                        break;
                    }
                }
            }

            if (playerSeqNum++ == MAX_AUDIO_SEQNO) playerSeqNum = 0;
            mSemaphore.release();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return outData;
    }
    public int size()
    {
        return mAudioDataQueue.size();
    }

    public void clear()
    {
        mAudioDataQueue.clear();
        mAudioDataQueue = null;
    }

    boolean isValidCheck(int seqNum, int distance)
    {
        boolean bNeedInsert = true;

        for(Iterator<AudioData>iter = mAudioDataQueue.iterator(); iter.hasNext();)
        {
            AudioData data = iter.next();
            int interSeq = data.seqNo;
            if(seqNum == data.seqNo) bNeedInsert = false; //바로 리턴해도 될듯?

            if(seqNum - MAX_DISTANCE < 0)
            {
                if(interSeq > seqNum + MAX_DISTANCE  && interSeq < (MAX_AUDIO_SEQNO -(MAX_DISTANCE-seqNum))) {
                    //Log.e(LOG_TAG, "currSeq " + seqNum + " skipSeq :" + interSeq);
                    iter.remove();
                }
            }
            else if (seqNum + MAX_DISTANCE > MAX_AUDIO_SEQNO)
            {
                if((seqNum - MAX_DISTANCE) > interSeq && interSeq > (MAX_DISTANCE - (MAX_AUDIO_SEQNO -seqNum))) {
                    //Log.e(LOG_TAG, "currSeq " + seqNum + " skipSeq :" + interSeq);
                    iter.remove();
                }
            }
            else
            {
                if((seqNum - MAX_DISTANCE) > interSeq || interSeq > seqNum+MAX_DISTANCE) {
                     //Log.e(LOG_TAG, "currSeq " + seqNum + " skipSeq :" + interSeq);
                    iter.remove();
                }
            }
        }
        return bNeedInsert;
    }

    int getFirstPacketNo()
    {
        int firstNum=0;
        boolean isFirst=true;

        for(Iterator<AudioData>iter = mAudioDataQueue.iterator(); iter.hasNext();) {
            AudioData data = iter.next();
            int interSeq = data.seqNo;
            if(isFirst)
            {
                firstNum = interSeq;
                isFirst= false;
            }
            if(interSeq > MAX_AUDIO_SEQNO - MAX_DISTANCE){
                if(firstNum < MAX_DISTANCE)
                    firstNum  = interSeq;
                else if(interSeq < firstNum)
                    firstNum = interSeq;
            }
            else if(interSeq < firstNum)
                firstNum = interSeq;
        }
        Log.e(LOG_TAG, "firstNum " + firstNum);
        return firstNum;
    }
    boolean checkDistanceError()
    {
        if(playerSeqNum - MAX_DISTANCE < 0)
        {
            if(receiveSeqNum > playerSeqNum + MAX_DISTANCE  && receiveSeqNum < (MAX_AUDIO_SEQNO -(MAX_DISTANCE-playerSeqNum))) {
                Log.e(LOG_TAG, "Distance Error playerSeqNum " + playerSeqNum + " receiveSeqNum :" + receiveSeqNum);
               return true;
            }
        }
        else if (playerSeqNum + MAX_DISTANCE > MAX_AUDIO_SEQNO)
        {
            if((playerSeqNum - MAX_DISTANCE) > receiveSeqNum && receiveSeqNum > (MAX_DISTANCE - (MAX_AUDIO_SEQNO -playerSeqNum))) {
                Log.e(LOG_TAG, "Distance Error playerSeqNum " + playerSeqNum + " receiveSeqNum :" + receiveSeqNum);
                return true;
            }
        }
        else
        {
            if((playerSeqNum - MAX_DISTANCE) > receiveSeqNum || receiveSeqNum > playerSeqNum+MAX_DISTANCE) {
                Log.e(LOG_TAG, "Distance Error playerSeqNum " + playerSeqNum + " receiveSeqNum :" + receiveSeqNum);
                return true;
            }
        }
        return false;
    }

    AudioData getDataBySeqNo(int targetNo)
    {
        for (Iterator<AudioData> iter = mAudioDataQueue.iterator(); iter.hasNext(); ) {
            AudioData data = iter.next();
            if (data.seqNo == targetNo) {
                return data;
            }
        }
        return null;
    }
}
