package com.romanpulov.symphonytimer.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.activity.MainActivity;
import com.romanpulov.symphonytimer.model.DMTasks;

import java.lang.ref.WeakReference;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.romanpulov.symphonytimer.common.NotificationRepository.NOTIFICATION_ID_ONGOING;

/**
 * Created by rpulov on 06.11.2016.
 */

public class NotificationHelper {
    private void log(String message) {
        LoggerHelper.logContext(mContext.get(), "NotificationHelper", message);
    }

    private static String CHANNEL_DEFAULT_IMPORTANCE = "Default importance channel";

    private static NotificationHelper mNotificationHelper;

    public static NotificationHelper getInstance(Context context) {
        if (mNotificationHelper == null) {
            mNotificationHelper = new NotificationHelper(context);
            createNotificationChannel(context);
        }
        return mNotificationHelper;
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
                    CHANNEL_DEFAULT_IMPORTANCE,
                    CHANNEL_DEFAULT_IMPORTANCE,
                    NotificationManager.IMPORTANCE_DEFAULT);
            defaultChannel.setDescription(CHANNEL_DEFAULT_IMPORTANCE);
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

        public NotificationInfo(String content, int progress) {
            mContent = content;
            mProgress = progress;
        }

        public void updateNotificationInfo(String content, int progress) {
            mModified = !content.equals(mContent) || mProgress != progress;
            mContent = content;
            mProgress = progress;
        }
    }

    private NotificationHelper(Context context) {
        mContext = new WeakReference<>(context);
        mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, MainActivity.class);
        mContentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
    }

    public Notification getNotification(DMTasks dmTasks) {
        if (mContext.get() != null) {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(mContext.get(), CHANNEL_DEFAULT_IMPORTANCE)
                            .setSmallIcon(R.drawable.wait_notification)
                            .setAutoCancel(false)
                            .setOngoing(true)
                            .setContentTitle(mContext.get().getString(R.string.app_name))
                            .setContentText(dmTasks.getTaskTitles())
                            .setProgress(100, dmTasks.getExecutionPercent(), false)
                            .setContentIntent(mContentIntent)
                            .setOnlyAlertOnce(true)
                    ;
            return builder.build();
        } else {
            log("No context");
            return null;
        }
    }

    public void notify(DMTasks dmTasks) {
        // get modification info
        if (mNotificationInfo == null)
            mNotificationInfo = new NotificationInfo(dmTasks.getTaskTitles(), dmTasks.getExecutionPercent());
        else
            mNotificationInfo.updateNotificationInfo(dmTasks.getTaskTitles(), dmTasks.getExecutionPercent());

        if (mNotificationInfo.isModified()) {
            Notification notification = getNotification(dmTasks);
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
