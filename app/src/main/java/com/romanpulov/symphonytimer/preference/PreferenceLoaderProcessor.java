package com.romanpulov.symphonytimer.preference;

/**
 * Preference loader processor interface
 * Created by romanpulov on 21.11.2017.
 */

public interface PreferenceLoaderProcessor {
    void preExecute();
    void postExecute(String result);
}
