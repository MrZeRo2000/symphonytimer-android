package com.romanpulov.symphonytimer.cloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.romanpulov.library.gdrive.GDBaseHelper;
import com.romanpulov.library.gdrive.GDConfig;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;

public class GDHelper extends GDBaseHelper {
    private static final int REQUEST_CODE_SIGN_IN = 8423;

    @Override
    protected void configure() {
        GDConfig.configure(R.raw.gd_config, "https://www.googleapis.com/auth/drive", REQUEST_CODE_SIGN_IN);
    }

    private static GDHelper instance;

    private GDHelper() {
        super();
    }

    public static GDHelper getInstance() {
        if (instance == null) {
            instance = new GDHelper();
        }
        return instance;
    }

    public static void handleActivityResult(Context context, int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener(account -> {
                        PreferenceRepository.displayMessage(context, R.string.notification_gdrive_successfully_logged_in);
                        GDHelper.getInstance().setServerAuthCode(account.getServerAuthCode());
                    })
                    .addOnFailureListener(e -> {
                        PreferenceRepository.displayMessage(context, R.string.error_onedrive_login, e.getMessage());
                    });
        }
    }

}
