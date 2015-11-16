package com.example.robert.together;

import android.app.Application;
import android.content.Context;

/**
 * Created by robert on 11/10/15.
 */
public class TogetherApp extends Application {
    private static Context sContext;

    public static Context getContext() {
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (sContext == null) {
            synchronized (TogetherApp.class) {
                sContext = getApplicationContext();
            }
        }
    }
}
