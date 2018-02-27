package com.romanpulov.symphonytimer.helper;

import android.content.Context;

/**
 * Helper class for managing timer signals - sound, vibration
 * Created by romanpulov on 26.02.2018.
 */

public final class TimerSignalHelper {
    private void log(String message) {
        LoggerHelper.getInstance(mContext).log("TimerSignalHelper", message);
    }

    public static final int STATUS_OFF = 0;
    public static final int STATUS_ON = 1;

    private final Context mContext;
    private final MediaPlayerHelper mMediaPlayerHelper;
    private final MediaRecorderHelper mMediaRecorderHelper;

    private int mStatus = STATUS_OFF;
    private long mStartTime;

    public boolean isStatusOn() {
        return mStatus == STATUS_ON;
    }

    public long getDuration() {
        if (mStatus == STATUS_ON)
            return System.currentTimeMillis() - mStartTime;
        else
            return 0;
    }

    public int getDurationSeconds() {
        return (int)(getDuration() / 1000);
    }

    public TimerSignalHelper(Context context) {
        mContext = context;
        mMediaPlayerHelper = new MediaPlayerHelper(context);
        mMediaRecorderHelper = new MediaRecorderHelper(context);
    }

    public void setSoundFileName(String soundFileName) {
        mMediaPlayerHelper.setSoundFileName(soundFileName);
    }

    public void changeSoundFileName(String soundFileName) {
        mMediaPlayerHelper.stop();
        mMediaPlayerHelper.setSoundFileName(soundFileName);
        mMediaPlayerHelper.start();
    }

    public void start() {
        log("started");
        mStatus = STATUS_ON;
        mStartTime = System.currentTimeMillis();

        mMediaPlayerHelper.start();
        VibratorHelper.vibrate(mContext);
    }

    public void stop() {
        log("stopped");
        mStatus = STATUS_OFF;

        VibratorHelper.cancel(mContext);
        mMediaPlayerHelper.stop();
    }
}
