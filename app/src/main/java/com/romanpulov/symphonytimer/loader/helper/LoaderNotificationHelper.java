package com.romanpulov.symphonytimer.loader.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.activity.SettingsActivity;

import java.text.DateFormat;
import java.util.Date;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Notification helper for loader operations
 * Created by romanpulov on 07.03.2018.
 */

public final class LoaderNotificationHelper {

    private static final String CHANNEL_LOADER_ID = "LOADER_CHANNEL";
    private static final String CHANNEL_LOADER_NAME = "Loader channel";
    private static final String CHANNEL_LOADER_DESCRIPTION = "Channel for loader notifications";

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
                    CHANNEL_LOADER_ID,
                    CHANNEL_LOADER_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            defaultChannel.setDescription(CHANNEL_LOADER_DESCRIPTION);
            defaultChannel.setSound(null, null);

            // Register the successChannel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(defaultChannel);
        }
    }

    public static void notify(Context context, String message, int notificationId) {
        checkNotificationChannel(context);

        Intent intent = new Intent(context, SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_LOADER_ID)
                        .setSmallIcon(R.drawable.wait_notification_white)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(message)
                        .setSubText(DateFormat.getDateTimeInstance().format(new Date()))
                        .setContentIntent(pendingIntent)
                        .setPriority(Notification.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        ;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.notify(notificationId, builder.build());
    }
}
