package com.romanpulov.symphonytimer.loader.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.activity.SettingsActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Notification helper for loader operations
 * Created by romanpulov on 07.03.2018.
 */

public final class LoaderNotificationHelper {
    public static void notify(Context context, String message, int notificationId) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification.Builder builder =
                new Notification.Builder(context)
                        .setSmallIcon(R.drawable.wait_notification)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(message)
                        .setContentIntent(pendingIntent)
                        .setPriority(Notification.PRIORITY_DEFAULT)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setAutoCancel(true)
                        ;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.notify(notificationId, builder.build());
    }
}
