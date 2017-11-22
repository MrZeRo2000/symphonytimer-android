package com.romanpulov.symphonytimer.preference;

import com.romanpulov.library.common.loader.core.Loader;

/**
 * Preference loader processor interface
 * Created by romanpulov on 21.11.2017.
 */

public interface PreferenceLoaderProcessor {
    void preExecute();
    void postExecute(String result);
    Class<? extends Loader> getLoaderClass();
}
