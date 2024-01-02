package com.romanpulov.symphonytimer.common;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;

import android.os.Build;

/**
 * Common notification repository class
 * Created by romanpulov on 07.03.2018.
 */

public final class NotificationRepository {

    public static final int NOTIFICATION_ID_ONGOING;
    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            NOTIFICATION_ID_ONGOING = FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
        } else {
            NOTIFICATION_ID_ONGOING = 1;
        }
    }
    public static final int NOTIFICATION_ID_LOADER = 2;
}
