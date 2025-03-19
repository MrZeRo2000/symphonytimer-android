package com.romanpulov.symphonytimer.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.activity.MainActivity;

import java.lang.ref.WeakReference;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.romanpulov.symphonytimer.common.NotificationRepository.NOTIFICATION_ID_ONGOING;

/**
 * Created by rpulov on 06.11.2016.
 */

public class ProgressNotificationHelper {
    private void log(String message) {
        LoggerHelper.logContext(mContext.get(), "ProgressNotificationHelper", message);
    }

    private static final String CHANNEL_PROGRESS_ID = "PROGRESS_CHANNEL";
    private static final String CHANNEL_PROGRESS_NAME = "Progress channel";
    private static final String CHANNEL_PROGRESS_DESCRIPTION = "Channel for progress notification";

    private static ProgressNotificationHelper mProgressNotificationHelper;

    public static ProgressNotificationHelper getInstance(Context context) {
        if (mProgressNotificationHelper == null) {
            mProgressNotificationHelper = new ProgressNotificationHelper(context);
            createNotificationChannel(context);
        }
        return mProgressNotificationHelper;
    }

    private final WeakReference<Context> mContext;
    private final NotificationManager mNotificationManager;
    private final PendingIntent mContentIntent;
    private NotificationInfo mNotificationInfo;

    private static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel defaultChannel = new NotificationChannel(
                    CHANNEL_PROGRESS_ID,
                    CHANNEL_PROGRESS_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            defaultChannel.setDescription(CHANNEL_PROGRESS_DESCRIPTION);
            defaultChannel.setSound(null, null);

            // Register the successChannel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(defaultChannel);
        }
    }

    private static class NotificationInfo {
        private String mContent;
        private int mProgress;
        private boolean mModified = true;

        public boolean isModified() {
            return mModified;
        }

        NotificationInfo(String content, int progress) {
            mContent = content;
            mProgress = progress;
        }

        void updateNotificationInfo(String content, int progress) {
            mModified = !content.equals(mContent) || mProgress != progress;
            mContent = content;
            mProgress = progress;
        }
    }

    private ProgressNotificationHelper(Context context) {
        mContext = new WeakReference<>(context);
        mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, MainActivity.class);
        mContentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
    }

    public Notification getNotification(String titles, int executionPercent) {
        if (mContext.get() != null) {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(mContext.get(), CHANNEL_PROGRESS_ID)
                            .setSmallIcon(R.drawable.wait_notification_white)
                            .setAutoCancel(false)
                            .setOngoing(true)
                            .setContentTitle(mContext.get().getString(R.string.app_name))
                            .setContentText(titles)
                            .setProgress(100, executionPercent, false)
                            .setContentIntent(mContentIntent)
                            .setOnlyAlertOnce(true)
                    ;
            return builder.build();
        } else {
            log("No context");
            return null;
        }
    }

    public void notify(String titles, int executionPercent) {
        // get modification info
        if (mNotificationInfo == null)
            mNotificationInfo = new NotificationInfo(titles, executionPercent);
        else
            mNotificationInfo.updateNotificationInfo(titles, executionPercent);

        if (mNotificationInfo.isModified()) {
            Notification notification = getNotification(titles, executionPercent);
            if (notification != null)
                mNotificationManager.notify(NOTIFICATION_ID_ONGOING, notification);
        } else {
            log("No change, skipping notification");
        }
    }

    public void cancel() {
        mNotificationManager.cancel(NOTIFICATION_ID_ONGOING);
    }
}
