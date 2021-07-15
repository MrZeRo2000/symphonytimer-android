package com.romanpulov.symphonytimer.cloud;

import com.romanpulov.library.msgraph.MSALConfig;
import com.romanpulov.library.msgraph.MSGraphBaseHelper;
import com.romanpulov.symphonytimer.R;

public class MSGraphHelper extends MSGraphBaseHelper {
    @Override
    protected void configure() {
        MSALConfig.configure(R.raw.auth_config_single_account, "Files.ReadWrite.All".toLowerCase().split(" "));
    }

    private static MSGraphHelper instance;

    public static MSGraphHelper getInstance() {
        if (instance == null) {
            instance = new MSGraphHelper();
            instance.configure();
        }

        return instance;
    }

    private MSGraphHelper() {

    }
}
