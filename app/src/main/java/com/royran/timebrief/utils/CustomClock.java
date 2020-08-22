package com.royran.timebrief.utils;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

public abstract class CustomClock {
    private final static String TAG = "CustomClock";
    private final static int TIME_COUNTING = 1;
    private long mStartTime;
    private long mCountIntervalMillis;
    private boolean mCancelled;

    private Handler mHandler;

    public CustomClock(long countIntervalMillis) {
        mStartTime = 0;
        mCountIntervalMillis = countIntervalMillis;
        mHandler = new UiHandler();
    }

    public synchronized final CustomClock start() {
        if (mCountIntervalMillis <= 0) {
            return this;
        }
        mCancelled = false;
        mStartTime = SystemClock.elapsedRealtime();
        mHandler.sendMessage(mHandler.obtainMessage(TIME_COUNTING));
        return this;
    }

    public synchronized final void cancel() {
        if (!mCancelled) {
            mCancelled = true;
            mHandler.removeMessages(TIME_COUNTING);
        }
    }
    
    public abstract void onTick(long millisIncreased);

    private class UiHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            synchronized (CustomClock.this) {
                if (mCancelled || msg.what != TIME_COUNTING) {
                    return;
                }
                final long millisIncreased = SystemClock.elapsedRealtime() - mStartTime;
                long lastTickStart = SystemClock.elapsedRealtime();
                onTick(millisIncreased);
                // take into account user's onTick taking time to execute
                long lastTickDuration = SystemClock.elapsedRealtime() - lastTickStart;
                long delay = mCountIntervalMillis - lastTickDuration;
                // special case: user's onTick took more than interval to
                // complete, skip to next interval
                while (delay < 0) delay += mCountIntervalMillis;
                sendMessageDelayed(obtainMessage(TIME_COUNTING), delay);
            }
        }
    }
}
