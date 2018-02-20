package com.romanpulov.symphonytimer.helper;

import android.content.Context;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

/**
 * Helper class for MediaRecorder
 * Created by romanpulov on 20.02.2018.
 */

public final class MediaRecorderHelper {
    private static void logContext(Context context, String message) {
        LoggerHelper.logContext(context, "MediaRecorderHelper", message);
    }


    private final Context mContext;
    private MediaRecorder mMediaRecorder;
    private File mMediaRecordFile;
    private boolean mIsRecording = false;
    private double mInitialAmplitude;
    private double mCurrentAmplitude;
    private Thread mThread;
    private boolean mThreadNeedTerminate = false;

    private static double TO_DECIBELS(int value) {
        if (value == 0)
            return 0;
        else
            return 20 * Math.log10(value);
    }

    public double getMaxAmplitude() {
        return TO_DECIBELS(mMediaRecorder.getMaxAmplitude());
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public void setMediaRecordFile(File file) {
        mMediaRecordFile = file;
    }

    public MediaRecorderHelper(Context context) {
        mContext = context;
    }

    private boolean startRecording() {
        if (mIsRecording || (mMediaRecordFile == null))
            return false;

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile(mMediaRecordFile.getAbsolutePath());

        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();

            mInitialAmplitude = getMaxAmplitude();
            logContext(mContext, "Initial amplitude : " + mInitialAmplitude);
            logContext(mContext, "writing to file : " + mMediaRecordFile.getAbsolutePath());

            mIsRecording = true;
            return true;
        } catch (IOException e) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            e.printStackTrace();
        }

        return false;
    }

    private void stopRecording() {
        if (mIsRecording && (mMediaRecorder != null)) {
            try {
                logContext(mContext, "Stopping");
                mMediaRecorder.stop();
                mMediaRecorder.release();

                logContext(mContext, "Deleting file");
                boolean deleteResult = mMediaRecordFile.delete();
                if (deleteResult)
                    logContext(mContext, "File deleted");
                else
                    logContext(mContext, "File delete failed");
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                mMediaRecorder = null;
                mIsRecording = false;
            }
        }
    }

    public void startRecordingThread() {
        mThreadNeedTerminate = false;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                startRecording();

                while (!mThreadNeedTerminate) {
                    try {
                        mCurrentAmplitude = getMaxAmplitude();
                        logContext(mContext, "Current amplitude : " + mCurrentAmplitude);

                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                stopRecording();
            }
        });
        mThread.start();
    }

    public void stopRecordingThread() {
        mThreadNeedTerminate = true;
    }
}

