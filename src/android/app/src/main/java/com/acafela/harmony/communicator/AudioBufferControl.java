package com.acafela.harmony.communicator;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.acafela.harmony.crypto.ICrypto;
import com.acafela.harmony.service.HarmonyService;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import static com.acafela.harmony.ui.TestCallActivity.INTENT_CONTROL;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_SIP_DATA_TIMEOUT;

public class AudioBufferControl {
    private static final String LOG_TAG = "AudioBufferControl";
    private int MAX_DISTANCE=20;
    private int playerSeqNum;
    private int receiveSeqNum;
    //private ICrypto mCrypto;
    private Context mContext;
    private int MAX_AUDIO_SEQNO;
    private ArrayList mAudioDataQueue;
    private boolean isFirstFeeding;
    static Semaphore mSemaphore;
    private AudioData backupData = new AudioData();
    private int  lossCount = 0;
    private int  monitorCont = 0;
    private int  PERIOD_LOSS_CHECK_TIME = 50*10;

    private int  MAX_LOSS_NUM = PERIOD_LOSS_CHECK_TIME* 50 /100;


    AudioBufferControl(Context context,int maxBufferSize)
    {
        mAudioDataQueue = new ArrayList<AudioData>();
        playerSeqNum = 0;
        mContext = context;
        MAX_AUDIO_SEQNO = maxBufferSize;
        isFirstFeeding = true;
        mSemaphore= new Semaphore(1);
    }

    public void pushData(AudioData data)  {
        try {
            mSemaphore.acquire();
            //isDistanceCheck(data.seqNo,MAX_DISTANCE);
            chececkExpiredData();
            //Log.e(LOG_TAG, "<<<<< currSeq " + data.seqNo );
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
                    playerSeqNum = getOldDataSeqNo(true);
                    isFirstFeeding = false;
                } else {
                    mSemaphore.release();
                    return null;
                }
            }

            //Exception : 범위가 벗어난경우 가장오래된 데이터로 초기화
            if (checkDistanceError()) {
                //playerSeqNum = getFirstPacketNo();
                playerSeqNum = getOldDataSeqNo(false);
            }

            //현재 seqnumber 재생
            outData = getDataBySeqNo(playerSeqNum);
            if(outData!=null) {
                backupData.seqNo = outData.seqNo;
                backupData.data = outData.data.clone();
                // Log.e(LOG_TAG, "currSeq " + outData.seqNo);
            }
            else {
                // 없는경우 마지막 data로 재생
                lossCount++;
                //Log.e(LOG_TAG, "LOSS : " + lossCount + " TOTAL :" + MAX_LOSS_NUM);
                Log.e(LOG_TAG, "[LOSS]BackupData :" + backupData.seqNo + " playerSeqNum : " + playerSeqNum +  " receiver : " + receiveSeqNum);
                outData = backupData;
            }

            if(monitorCont++ >PERIOD_LOSS_CHECK_TIME) {
                monitorCont = 0;
                lossCount = 0;
            } else if(lossCount > MAX_LOSS_NUM) {
                Log.e(LOG_TAG, "DATA TIMEOUT ERROR");
                Intent intent = new Intent(mContext, HarmonyService.class);
                intent.putExtra(INTENT_CONTROL, INTENT_SIP_DATA_TIMEOUT);
                mContext.startService(intent);
                monitorCont = 0;
                lossCount = 0;
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
        int checkCnt=0;
        for (Iterator<AudioData> iter = mAudioDataQueue.iterator(); iter.hasNext(); ) {
            AudioData data;
            try {
                data = iter.next();
            } catch (ConcurrentModificationException e)
            {
                e.printStackTrace();
                return null;
            }
            checkCnt++;
            if (data.seqNo == targetNo) {
                if(checkCnt>MAX_DISTANCE-5) {
                    try {
                        Thread.sleep(150);
                        Log.i(LOG_TAG,"need Delay...");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
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
    int getOldDataSeqNo(boolean isForce)
    {
        AudioData data;
        if(mAudioDataQueue.size()==0)
        {
            Log.e(LOG_TAG, "there is no data ");
            return playerSeqNum;
        }
        if(isForce==false && mAudioDataQueue.size() >MAX_DISTANCE/2)
        {
            data = (AudioData)mAudioDataQueue.get(mAudioDataQueue.size()/2);
            Log.e(LOG_TAG, "Middle data SeqNo : " + data.seqNo);
        } else {
            data = (AudioData) mAudioDataQueue.get(0);
            Log.e(LOG_TAG, "Oldest data SeqNo : " + data.seqNo);
        }
        return data.seqNo;
    }
    boolean isExistNextPacket()
    {
        for(Iterator<AudioData>iter = mAudioDataQueue.iterator(); iter.hasNext();)
        {
            AudioData data = iter.next();

            if(data.seqNo > playerSeqNum){
                /*Log.e(LOG_TAG, "===================>check  : " + data.seqNo +" player : " + playerSeqNum +" receiver : " + receiveSeqNum);
                for(Iterator<AudioData>iter2 = mAudioDataQueue.iterator(); iter2.hasNext();)
                {
                    AudioData data2 = iter2.next();
                    Log.i(LOG_TAG, "data  : " + data2.seqNo);
                }*/
                return true;
            }
            if(data.seqNo - MAX_DISTANCE < 0 )
                if(data.seqNo + MAX_AUDIO_SEQNO > playerSeqNum){
                    //Log.e(LOG_TAG, "Oldest data SeqNo : " + data.seqNo);
                    return true;
                }
        }
        return false;
    }
}
