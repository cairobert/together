package com.example.robert.together;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by robert on 11/4/15.
 */
public class Toaster {
    public static final int ERR_LOCATION_UNKOWN = 9;
    private Handler mUiHandler;
    private static Toaster sToaster;
    private Context mContext;

    public static Toaster getInstance(Context ctx) {
        if (sToaster == null) {
            synchronized (Toaster.class) {
                sToaster = new Toaster(ctx);
            }
        }
        return sToaster;
    }

    public Toaster(Context ctx) {
        mUiHandler = new Handler(ctx.getApplicationContext().getMainLooper());
        mContext = ctx;
    }

    public void showToast(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    public void showToast(final String msg, final int length) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, msg, length).show();
            }
        });
    }

    public void showToast(int errCode) {
        if (errCode == ERR_LOCATION_UNKOWN) {
            showToast("不能获取您当前的位置");
        } else {
            showToast("获取信息失败");
        }
    }
}
