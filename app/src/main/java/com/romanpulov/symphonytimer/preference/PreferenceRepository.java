package com.romanpulov.symphonytimer.preference;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.romanpulov.symphonytimer.R;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by romanpulov on 21.11.2017.
 */

public final class PreferenceRepository {
    public static final long PREF_LOAD_NEVER = 0;
    public static final long PREF_LOAD_LOADING = 1;
    public static final long PREF_LOAD_CURRENT_VALUE = 2;

    public static final String PREF_KEY_DROPBOX_BACKUP =  "pref_dropbox_backup";
    public static final String PREF_KEY_DROPBOX_BACKUP_LAST_LOADED =  "pref_dropbox_backup_last_loaded";

    public static final String PREF_KEY_DROPBOX_RESTORE =  "pref_dropbox_restore";
    public static final String PREF_KEY_DROPBOX_RESTORE_LAST_LOADED =  "pref_dropbox_restore_last_loaded";


    /**
     * Display message common routine
     * @param preferenceFragment PreferenceFragment
     * @param message Message to display
     */
    public static void displayMessage(PreferenceFragment preferenceFragment, CharSequence message) {
        Toast.makeText(preferenceFragment.getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    public static void updateDropboxBackupPreferenceSummary(PreferenceFragment preferenceFragment, long value) {
        updateLoadPreferenceSummary(preferenceFragment, PREF_KEY_DROPBOX_BACKUP, PREF_KEY_DROPBOX_BACKUP_LAST_LOADED, value);
    }

    public static void updateLoadPreferenceSummary(PreferenceFragment preferenceFragment, String preferenceKey, String preferenceLastLoadedKey, long value) {
        Preference prefLoad = preferenceFragment.findPreference(preferenceKey);

        if (value == PREF_LOAD_LOADING)
            prefLoad.setSummary(R.string.caption_loading);
        else {
            long displayValue = prefLoad.getPreferenceManager().getSharedPreferences().getLong(preferenceLastLoadedKey, PreferenceRepository.PREF_LOAD_NEVER);
            if (displayValue == PREF_LOAD_NEVER)
                prefLoad.setSummary(R.string.pref_message_last_loaded_never);
            else
                prefLoad.setSummary(String.format(
                        prefLoad.getContext().getResources().getString(R.string.pref_message_last_loaded_format),
                        DateFormat.getDateTimeInstance().format(new Date(displayValue))));
        }
    }

    private static void setPreferenceLong(Context context, String key, long value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, value).apply();
    }

    /**
     * Sets last loaded backup time
     * @param context Context for preferences
     * @param loadedTime last loaded time
     */
    public static void setDropboxBackupLastLoadedTime(Context context, long loadedTime) {
        setPreferenceLong(context, PreferenceRepository.PREF_KEY_DROPBOX_BACKUP_LAST_LOADED, loadedTime);
    }

    /**
     * Sets last loaded backup time to current time
     * @param context Context for preferences
     */
    public static void setDropboxBackupLastLoadedCurrentTime(Context context) {
        long loadedTime = System.currentTimeMillis();
        setDropboxBackupLastLoadedTime(context, loadedTime);
    }

    /**
     * Sets last loaded backup time
     * @param context Context for preferences
     * @param loadedTime last loaded time
     */
    public static void setDropboxRestoreLastLoadedTime(Context context, long loadedTime) {
        setPreferenceLong(context, PreferenceRepository.PREF_KEY_DROPBOX_RESTORE_LAST_LOADED, loadedTime);
    }

    /**
     * Sets last loaded restore time to current time
     * @param context Context for preferences
     */
    public static void setDropboxRestoreLastLoadedCurrentTime(Context context) {
        long loadedTime = System.currentTimeMillis();
        setDropboxRestoreLastLoadedTime(context, loadedTime);
    }

    public static void updateDropboxRestorePreferenceSummary(PreferenceFragment preferenceFragment, long value) {
        updateLoadPreferenceSummary(preferenceFragment, PREF_KEY_DROPBOX_RESTORE, PREF_KEY_DROPBOX_RESTORE_LAST_LOADED, value);
    }

    public static void setDropboxRestoreDefaultPreferenceSummary(PreferenceFragment preferenceFragment) {
        Preference pref = preferenceFragment.findPreference(PREF_KEY_DROPBOX_RESTORE);
        pref.setSummary(R.string.pref_summary_dropbox_restore);
    }
}
