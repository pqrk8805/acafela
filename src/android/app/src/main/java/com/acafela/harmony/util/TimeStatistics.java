package com.acafela.harmony.util;

import android.util.Log;

public class TimeStatistics {
    private static final String TAG = TimeStatistics.class.getName();
    private final boolean mUseTimeStatistics = false;
    private final boolean mUseNanoSeconds = true;

    private String mFunctionName;
    private long mTotalTime;
    private long mCount;
    private long mStartTime;
    private long mMaxTime;
    private long mMinTime;

    public void init(String name) {
        if (!mUseTimeStatistics) {
            return;
        }
        mTotalTime = 0;
        mCount = 0;
        mMinTime = 99999999;
        mMaxTime = 0;
        mFunctionName = name;
    }

    public void timeCheckStart() {
        if (!mUseTimeStatistics) {
            return;
        }
        if (mUseNanoSeconds) {
            mStartTime = System.nanoTime();
        }
        else {
            mStartTime = System.currentTimeMillis();
        }
    }

    public void timeCheckFinish() {
        if (!mUseTimeStatistics) {
            return;
        }
        mCount++;
        long finishTime;
        if (mUseNanoSeconds) {
            finishTime = System.nanoTime();
        }
        else {
            finishTime = System.currentTimeMillis();
        }
        long executionTime = finishTime - mStartTime;
        mTotalTime += executionTime;

        if (executionTime<mMinTime) {
            mMinTime = executionTime;
        }
        if (executionTime>mMaxTime) {
            mMaxTime = executionTime;
        }
    }

    public void finish() {
        if (!mUseTimeStatistics || mCount==0) {
            return;
        }

        if (mUseNanoSeconds) {
            Log.e(TAG, "Total Execution Time (" + mFunctionName + "): " + mTotalTime/1000000.0 + "ms");
            Log.e(TAG, "Total Execution Count (" + mFunctionName + "): " + mCount);
            Log.e(TAG, "Min Execution Time (" + mFunctionName + "): " + mMinTime/1000000.0 + "ms");
            Log.e(TAG, "Max Execution Time (" + mFunctionName + "): " + mMaxTime/1000000.0 + "ms");
            Log.e(TAG, "Average Execution Time (" + mFunctionName + "): " +
                    mTotalTime/mCount/1000000.0 + "ms");
        }
        else {
            Log.e(TAG, "Total Execution Time (" + mFunctionName + "): " + mTotalTime + "ms");
            Log.e(TAG, "Total Execution Count (" + mFunctionName + "): " + mCount);
            Log.e(TAG, "Min Execution Time (" + mFunctionName + "): " + mMinTime + "ms");
            Log.e(TAG, "Max Execution Time (" + mFunctionName + "): " + mMaxTime + "ms");
            Log.e(TAG, "Average Execution Time (" + mFunctionName + "): " +
                    (double)mTotalTime/mCount + "ms");
        }
    }
}
