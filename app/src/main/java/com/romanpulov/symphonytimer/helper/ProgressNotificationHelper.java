package com.romanpulov.symphonytimer.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.activity.MainActivity;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.romanpulov.symphonytimer.common.NotificationRepository.NOTIFICATION_ID_ONGOING;

/**
 * Created by rpulov on 06.11.2016.
 */

public class ProgressNotificationHelper {
    private final static String TAG = ProgressNotificationHelper.class.getSimpleName();

    private static final String CHANNEL_PROGRESS_ID = "PROGRESS_CHANNEL";
    private static final String CHANNEL_PROGRESS_NAME = "Progress channel";
    private static final String CHANNEL_PROGRESS_DESCRIPTION = "Channel for progress notification";

    private static boolean mNotificationChannelNeeded = true;

    private static void checkNotificationChannel(Context context) {
        if (mNotificationChannelNeeded) {
            createNotificationChannel(context.getApplicationContext());
            mNotificationChannelNeeded = false;
        }
    }

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
        private long mProgress;
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

    private static NotificationInfo mLastNotificationInfo;

    public static Notification buildNotification(@NonNull Context context, String titles, int executionProgress) {
        Log.d(TAG, "Notification execution progress: " + executionProgress);
        checkNotificationChannel(context);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_PROGRESS_ID)
                        .setSmallIcon(R.drawable.wait_notification_white)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(titles + (executionProgress > 0 ? ": " + DateFormatterHelper.formatTimeSeconds(executionProgress) : ""))
                        .setContentIntent(pendingIntent)
                        .setOnlyAlertOnce(true)
                ;
        return builder.build();
    }

    public static void notify(Context context, String titles, int executionProgress) {
        // get modification info
        if (mLastNotificationInfo == null)
            mLastNotificationInfo = new NotificationInfo(titles, executionProgress);
        else
            mLastNotificationInfo.updateNotificationInfo(titles, executionProgress);

        if (mLastNotificationInfo.isModified()) {
            Notification notification = buildNotification(context, titles, executionProgress);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(NOTIFICATION_ID_ONGOING, notification);
            }
        }
    }

    public static void cancel() {
        mLastNotificationInfo = null;
    }
}
