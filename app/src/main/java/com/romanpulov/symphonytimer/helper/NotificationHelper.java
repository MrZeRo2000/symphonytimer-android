package com.romanpulov.symphonytimer.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.activity.MainActivity;
import com.romanpulov.symphonytimer.model.DMTasks;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by rpulov on 06.11.2016.
 */

public class NotificationHelper {
    public static final int ONGOING_NOTIFICATION_ID = 1;

    private static NotificationHelper mNotificationHelper;

    public static NotificationHelper getInstance(Context context) {
        if (mNotificationHelper == null) {
            mNotificationHelper = new NotificationHelper(context);
        }
        return mNotificationHelper;
    }

    private final Context mContext;
    private final NotificationManager mNotificationManager;
    private final Intent mNotificationIntent;
    private final PendingIntent mContentIntent;

    private NotificationHelper(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        mNotificationIntent = new Intent(mContext, MainActivity.class);
        mContentIntent = PendingIntent.getActivity(mContext, 0, mNotificationIntent, 0);
    }

    public Notification getNotification(DMTasks dmTasks) {
        Notification.Builder builder =
                new Notification.Builder(mContext)
                        .setSmallIcon(R.drawable.wait_notification)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setContentTitle(mContext.getString(R.string.app_name))
                        .setContentText(dmTasks.getTaskTitles())
                        .setProgress(100, dmTasks.getExecutionPercent(), false)
                        .setContentIntent(mContentIntent);
        return builder.build();
    }

    public void notify(DMTasks dmTasks) {
        mNotificationManager.notify(ONGOING_NOTIFICATION_ID, getNotification(dmTasks));
    }

    public void cancel() {
        mNotificationManager.cancel(ONGOING_NOTIFICATION_ID);
    }
}
