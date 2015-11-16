package com.example.robert.together;

import android.app.Application;
import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by robert on 10/23/15.
 */
public class ThreadManager {
    private static ThreadManager sManager;

    private ExecutorService mExecutorService;
    private Context mContext;

    public static ThreadManager getInstance(Context context) {
        if (sManager == null) {
            sManager = new ThreadManager(context);
        }
        return sManager;
    }

    private ThreadManager(Context context) {
        mContext = context;
        mExecutorService = Executors.newCachedThreadPool();
    }

    public void addRunnable(Runnable callback) {
        mExecutorService.execute(callback);
    }
}
