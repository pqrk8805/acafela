package com.acafela.harmony.util;

import android.util.Log;

public class AverageTimeCheck {
    private static final String TAG = AverageTimeCheck.class.getName();
    private final boolean mUseAverageTimeCheck = true;

    private String mFunctionName;
    private long mTotalTime;
    private long mCount;
    private long mStartTime;

    public void init(String name) {
        if (!mUseAverageTimeCheck) {
            return;
        }
        mTotalTime = 0;
        mCount = 0;
        mFunctionName = name;
    }

    public void timeCheckStart() {
        if (!mUseAverageTimeCheck) {
            return;
        }
        mStartTime = System.currentTimeMillis();
    }

    public void timeCheckFinish() {
        if (!mUseAverageTimeCheck) {
            return;
        }
        mCount++;
        mTotalTime += System.currentTimeMillis() - mStartTime;
    }

    public void finish() {
        if (!mUseAverageTimeCheck || mCount==0) {
            return;
        }
        Log.e(TAG, "Total execute Time (" + mFunctionName + "): " + mTotalTime + "ms");
        Log.e(TAG, "Total count (" + mFunctionName + "): " + mCount);
        Log.e(TAG, "Average execute Time (" + mFunctionName + "): " +
                (double)mTotalTime/mCount + "ms");
    }
}
