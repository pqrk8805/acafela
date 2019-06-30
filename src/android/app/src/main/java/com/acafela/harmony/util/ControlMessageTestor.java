package com.acafela.harmony.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.acafela.harmony.service.HarmonyService;

import static com.acafela.harmony.controller.VoipController.CONTROL_TIMEOUT;
import static com.acafela.harmony.controller.VoipController.RETRY_COUNT;
import static com.acafela.harmony.ui.TestCallActivity.INTEMT_CALLEE_PHONENUMBER;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_CONTROL;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_ISVIDEO;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_SIP_INVITE_CALL;
import static com.acafela.harmony.ui.TestCallActivity.INTENT_SIP_TERMINATE_CALL;

public class ControlMessageTestor {
    private static final String TAG = ControlMessageTestor.class.getName();
    private final boolean mUseTestor = false;

    private static ControlMessageTestor INSTANCE;

    private final String CALL_PHONENUMBER = "1112";

    private Context mContext;
    private int mTestCount;
    private int mSuccessCount;
    private Object mLock = new Object();

    public synchronized static ControlMessageTestor getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ControlMessageTestor();
        }
        return INSTANCE;
    }

    public void init(Context context, final int totalCount) {
        if (!mUseTestor) {
            return;
        }

        Log.i(TAG, "ControlMessageTestor is Started!!");
        mContext = context;
        mTestCount = 1;
        mSuccessCount = 0;

        Thread testThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mTestCount<=totalCount) {
                    Log.i(TAG, "mTestCount: " + mTestCount);
                    initVoiceCall();

                    int timeout = CONTROL_TIMEOUT*(RETRY_COUNT+1);
                    long tBefore=System.currentTimeMillis();
                    synchronized(mLock) {
                        try {
                            mLock.wait(timeout);
                        } catch (InterruptedException e) {
                        }
                    }
                    if ((System.currentTimeMillis() - tBefore) < timeout-20)
                    {
                        mSuccessCount++;
                        Log.i(TAG, "mSuccessCount: " + mSuccessCount);
                    }

                    Intent serviceIntent = new Intent(mContext, HarmonyService.class);
                    serviceIntent.putExtra(INTENT_CONTROL, INTENT_SIP_TERMINATE_CALL);
                    mContext.startService(serviceIntent);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mTestCount++;
                }

                printTestResult();
            }
        });
        testThread.start();

    }

    private void initVoiceCall() {
        Intent serviceIntent = new Intent(mContext, HarmonyService.class);
        serviceIntent.putExtra(INTENT_CONTROL, INTENT_SIP_INVITE_CALL);
        serviceIntent.putExtra(INTEMT_CALLEE_PHONENUMBER, CALL_PHONENUMBER);
        serviceIntent.putExtra(INTENT_ISVIDEO, false);
        mContext.startService(serviceIntent);
    }

    public void getAck() {
        if (!mUseTestor) {
            return;
        }
        Log.i(TAG, "getAck");
        synchronized(mLock) {
            mLock.notify();
        }
    }

    public void printTestResult() {
        mTestCount--;
        Log.i(TAG, "ControlMessageTestor is Finished!!");
        Log.i(TAG, "Test Count: " + mTestCount);
        Log.i(TAG, "Success: " + mSuccessCount);
        Log.i(TAG, "Success rate: " + 100.0*mSuccessCount/mTestCount);
    }
}
