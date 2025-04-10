package com.romanpulov.symphonytimer.preference;

import android.content.Context;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import android.widget.Toast;

import androidx.preference.PreferenceFragmentCompat;

import com.romanpulov.symphonytimer.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by romanpulov on 21.11.2017.
 */

public final class PreferenceRepository {
    public static final long PREF_LOAD_NEVER = 0;
    public static final long PREF_LOAD_LOADING = 1;
    public static final long PREF_LOAD_CURRENT_VALUE = 2;


    public static final String PREF_KEY_CLOUD_ACCOUNT_TYPE = "pref_cloud_account_type";

    public static final int PREF_CLOUD_ACCOUNT_TYPE_MSGRAPH = 2;
    public static final int PREF_CLOUD_ACCOUNT_TYPE_GDRIVE = 3;

    public static final String PREF_KEY_CLOUD_BACKUP = "pref_cloud_backup";
    public static final String PREF_KEY_CLOUD_BACKUP_LAST_LOADED = "pref_cloud_backup_last_loaded";

    public static final String PREF_KEY_CLOUD_RESTORE =  "pref_cloud_restore";
    private static final String PREF_KEY_CLOUD_RESTORE_LAST_LOADED =  "pref_cloud_restore_last_loaded";

    public static final String PREF_KEY_LOCAL_BACKUP =  "pref_local_backup";
    private static final String PREF_KEY_LOCAL_BACKUP_LAST_LOADED =  "pref_local_backup_last_loaded";

    public static final String PREF_KEY_LOCAL_RESTORE =  "pref_local_restore";
    private static final String PREF_KEY_LOCAL_RESTORE_LAST_LOADED =  "pref_local_restore_last_loaded";

    private static final Map<String, String> PREF_KEYS_LAST_LOADED = new HashMap<>();
    static
    {
        PREF_KEYS_LAST_LOADED.put(PREF_KEY_CLOUD_BACKUP, PREF_KEY_CLOUD_BACKUP_LAST_LOADED);
        PREF_KEYS_LAST_LOADED.put(PREF_KEY_CLOUD_RESTORE, PREF_KEY_CLOUD_RESTORE_LAST_LOADED);
        PREF_KEYS_LAST_LOADED.put(PREF_KEY_LOCAL_BACKUP, PREF_KEY_LOCAL_BACKUP_LAST_LOADED);
        PREF_KEYS_LAST_LOADED.put(PREF_KEY_LOCAL_RESTORE, PREF_KEY_LOCAL_RESTORE_LAST_LOADED);
    }

    /**
     * Display message common routine
     * @param preferenceFragment PreferenceFragment
     * @param message Message to display
     */
    public static void displayMessage(PreferenceFragmentCompat preferenceFragment, CharSequence message) {
        displayMessage(preferenceFragment.getActivity(), message);
    }

    /**
     * Display message common routine
     * @param context Context
     * @param message Message to display
     */
    public static void displayMessage(Context context, CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Display message common routine
     * @param context Context
     * @param displayMessageId String resource id
     */
    public static void displayMessage(Context context, int displayMessageId) {
        displayMessage(context, context.getString(displayMessageId));
    }

    /**
     *
     * @param context Context
     * @param displayMessageId Formatted string resource id
     * @param message Message used in formatting
     */
    public static void displayMessage(Context context, int displayMessageId, String message) {
        displayMessage(context, context.getString(displayMessageId, message));
    }

    /**
     * Updates load status as summary
     * @param preferenceFragment PreferenceFragment
     * @param preferenceKey preference key
     * @param preferenceLastLoadedKey preference key with last loaded time
     * @param value value to set: PREF_LOAD_LOADING - loading in progress, PREV_LOAD_NEVER - never loaded, otherwise last loaded time
     */
    private static void updateLoadPreferenceSummary(PreferenceFragmentCompat preferenceFragment, String preferenceKey, String preferenceLastLoadedKey, long value) {
        Preference prefLoad = Objects.requireNonNull(preferenceFragment.findPreference(preferenceKey));

        if (value == PREF_LOAD_LOADING)
            prefLoad.setSummary(R.string.caption_loading);
        else {
            long displayValue = Objects.requireNonNull(prefLoad.getPreferenceManager().getSharedPreferences())
                    .getLong(preferenceLastLoadedKey, PreferenceRepository.PREF_LOAD_NEVER);
            if (displayValue == PREF_LOAD_NEVER)
                prefLoad.setSummary(R.string.pref_message_last_loaded_never);
            else
                prefLoad.setSummary(String.format(
                        prefLoad.getContext().getResources().getString(R.string.pref_message_last_loaded_format),
                        DateFormat.getDateTimeInstance().format(new Date(displayValue))));
        }
    }

    /**
     * Sets preference long value
     * @param context Context
     * @param key key name
     * @param value value to set
     */
    private static void setPreferenceLong(Context context, String key, long value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, value).apply();
    }

    /**
     * Updates last loaded time by preference key
     * @param preferenceFragment PreferenceFragment
     * @param preferenceKey preference key
     * @param value value to set
     */
    public static void updatePreferenceKeySummary(PreferenceFragmentCompat preferenceFragment, String preferenceKey, long value) {
        updateLoadPreferenceSummary(preferenceFragment, preferenceKey, PREF_KEYS_LAST_LOADED.get(preferenceKey), value);
    }

    /**
     * Sets last loaded time as current time by preference key
     * @param context Context
     * @param preferenceKey preference key
     */
    public static void setPreferenceKeyLastLoadedCurrentTime(Context context, String preferenceKey) {
        long loadedTime = System.currentTimeMillis();
        setPreferenceKeyLastLoadedTime(context, preferenceKey, loadedTime);
    }

    /**
     * Sets last loaded time by preference key
     * @param context Context
     * @param preferenceKey preference key
     * @param loadedTime loaded time
     */
    private static void setPreferenceKeyLastLoadedTime(Context context, String preferenceKey, long loadedTime) {
        setPreferenceLong(context, PREF_KEYS_LAST_LOADED.get(preferenceKey), loadedTime);
    }
}
