package com.acafela.harmony.communicator;

import android.util.Log;

import com.acafela.harmony.crypto.ICrypto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

public class AudioBufferControl {
    private static final String LOG_TAG = "AudioBufferControl";
    private int MAX_DISTANCE=20;
    private int playerSeqNum;
    private int receiveSeqNum;
    private ICrypto mCrypto;
    private int MAX_AUDIO_SEQNO;
    private ArrayList mAudioDataQueue;
    private boolean isFirstFeeding;
    static Semaphore mSemaphore;
    private AudioData backupData = new AudioData();
    static int retrycnt=0;

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
            //isDistanceCheck(data.seqNo,MAX_DISTANCE);
            chececkExpiredData();
            //Log.e(LOG_TAG, "currSeq " + data.seqNo );
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
                if (mAudioDataQueue.size() == MAX_DISTANCE - 2) {
                    //playerSeqNum = getFirstPacketNo();
                    playerSeqNum = getOldDataSeqNo();
                    isFirstFeeding = false;
                } else {
                    mSemaphore.release();
                    return null;
                }
            }

            //Exception : 범위가 벗어난경우 가장오래된 데이터로 초기화
            if (checkDistanceError()) {
                //playerSeqNum = getFirstPacketNo();
                playerSeqNum = getOldDataSeqNo();
            }

            //현재 seqnumber 재생
            outData = getDataBySeqNo(playerSeqNum);
            if(outData!=null) {
                backupData.seqNo = outData.seqNo;
                backupData.data = outData.data.clone();
                // Log.e(LOG_TAG, "currSeq " + outData.seqNo);
            }
            else {
                if(isExistNextPacket()==false) {
                    if (retrycnt++ < 5) {
                        mSemaphore.release();
                        Thread.sleep(30);
                        return null;
                    } else
                        retrycnt = 0;
                }
                // 없는경우 마지막 data로 재생
                Log.e(LOG_TAG, "Loss Packet : " + playerSeqNum + " use BackupData :" + backupData.seqNo);
                outData = backupData;
            }

            //현재 seq 없는경우 이전거 재생
            /*if(outData == null) {
                int distance;
                for(distance = 1; distance < MAX_DISTANCE; distance++) {
                    int tempSeq = playerSeqNum - distance;
                    if(tempSeq < 0)
                        tempSeq = MAX_AUDIO_SEQNO - (distance - playerSeqNum);

                    outData = getDataBySeqNo(tempSeq);
                    if(outData!=null)
                    {
                        Log.e(LOG_TAG, "loss packet" + playerSeqNum + " recovery packet " + outData.seqNo);
                        break;
                    }
                }
                if(outData==null)
                Log.e(LOG_TAG, "loss packet" + playerSeqNum);
            }
            if(outData==null)
                Log.e(LOG_TAG, "loss packet" + playerSeqNum);*/

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

    void isDistanceCheck (int seqNum, int distance)
    {
        for(Iterator<AudioData>iter = mAudioDataQueue.iterator(); iter.hasNext();)
        {
            AudioData data = iter.next();
            int interSeq = data.seqNo;

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
    void chececkExpiredData()
    {
        if(mAudioDataQueue.size() > MAX_DISTANCE)
        {
            for(int cnt = mAudioDataQueue.size() - MAX_DISTANCE  ; cnt>0 ;cnt-- ){
                //Log.e(LOG_TAG, "mAudioDataQueue.size() " + mAudioDataQueue.size() + " cnt :" + cnt);
                //mAudioDataQueue.remove(mAudioDataQueue.size() -1);
                mAudioDataQueue.remove(0);
            }
        }
    }
    boolean isValidCheck(int seqNum)
    {
        for(Iterator<AudioData>iter = mAudioDataQueue.iterator(); iter.hasNext();)
        {
            AudioData data = iter.next();
            //Log.e(LOG_TAG, "seqNum() " +seqNum + " cnt :" + data.seqNo);
            if(seqNum == data.seqNo) return false;
        }
        return true;
    }
    int getOldDataSeqNo()
    {
        if(mAudioDataQueue.size()==0)
        {
            Log.e(LOG_TAG, "there is no data ");
            return playerSeqNum;
        }
        AudioData data = (AudioData) mAudioDataQueue.get(0);
        Log.e(LOG_TAG, "Oldest data SeqNo : " + data.seqNo);
        return data.seqNo;
    }
    boolean isExistNextPacket()
    {
        for(Iterator<AudioData>iter = mAudioDataQueue.iterator(); iter.hasNext();)
        {
            AudioData data = iter.next();

            if(data.seqNo > playerSeqNum)
                return true;

            if(data.seqNo - MAX_DISTANCE < 0 )
                if(data.seqNo + MAX_AUDIO_SEQNO > playerSeqNum)
                    return true;
        }
        return false;
    }
}
