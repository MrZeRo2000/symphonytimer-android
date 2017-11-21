package com.romanpulov.symphonytimer.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.romanpulov.library.common.loader.core.Loader;
import com.romanpulov.library.common.loader.core.LoaderFactory;

/**
 * Loader service
 * Created by romanpulov on 21.11.2017.
 */

public class LoaderService extends IntentService {
    public static final String SERVICE_PARAM_LOADER_NAME = "LoaderService.LoaderName";
    public static final String SERVICE_RESULT_INTENT_NAME = "LoaderServiceResult";
    public static final String SERVICE_RESULT_LOADER_NAME = "LoaderServiceResult.LoaderName";
    public static final String SERVICE_RESULT_ERROR_MESSAGE = "LoaderServiceResult.ErrorMessage";

    private static final boolean DEBUGGING = false;

    private static void log(String message) {
        if (DEBUGGING)
            Log.d("LoaderService", message);
    }

    private String mLoaderClassName;

    public LoaderService() {
        super("Loader service");
        log("Started service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
            mLoaderClassName = intent.getStringExtra(SERVICE_PARAM_LOADER_NAME);
            String errorMessage = null;
            log("onHandleEvent : " + mLoaderClassName);
            try {
                Loader loader = LoaderFactory.fromClassName(this, mLoaderClassName);
                if (loader != null) {
                    log("created loader : " + mLoaderClassName);
                    //Thread.sleep(5000);
                    loader.load();
                } else
                    errorMessage = "Failed to create loader : " + mLoaderClassName;
            } catch (Exception e) {
                errorMessage = e.getMessage();
            }
            log("onHandleEvent completed");
            Intent resultIntent = new Intent(SERVICE_RESULT_INTENT_NAME);
            resultIntent.putExtra(SERVICE_RESULT_LOADER_NAME, mLoaderClassName);

            if (errorMessage != null)
                resultIntent.putExtra(SERVICE_RESULT_ERROR_MESSAGE, errorMessage);

            broadcastManager.sendBroadcast(resultIntent);
        }
    }

    public class LoaderBinder extends Binder {
        public LoaderService getService() {
            return LoaderService.this;
        }
    }

    private final IBinder mBinder = new LoaderBinder();

    @Override
    public IBinder onBind(Intent intent) {
        log("onBind service");
        return mBinder;
    }

    public String getLoaderClassName() {
        return mLoaderClassName;
    }

}
