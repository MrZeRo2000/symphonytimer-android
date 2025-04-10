package com.romanpulov.symphonytimer.cloud;

import com.romanpulov.library.gdrive.GDBaseHelper;
import com.romanpulov.library.gdrive.GDConfig;
import com.romanpulov.symphonytimer.R;

public class GDHelper extends GDBaseHelper {
    @Override
    protected void configure() {
        GDConfig.configure(R.raw.gd_config, "https://www.googleapis.com/auth/drive");
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
}
