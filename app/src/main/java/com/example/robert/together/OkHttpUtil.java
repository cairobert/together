package com.example.robert.together;


import android.content.Context;

import com.squareup.okhttp.OkHttpClient;

/**
 * Created by robert on 11/1/15.
 */
public class OkHttpUtil {
    private static OkHttpClient sClient;
    public static OkHttpClient getClient(Context ctx) {
        if (sClient == null) {
            synchronized (OkHttpUtil.class) {
                sClient = new OkHttpClient();
            }
        }
        return sClient;
    }
}
