package com.example.robert.together;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by robert on 10/20/15.
 */
public class ImageDownloader<T> extends HandlerThread {
    private static final String TAG = "ImageDownloader";
    private static final int THUMNAIL_DOWNLAOD = 0;

    // response handler must be attached to main thread. it is used to update ui
    private Handler mResponseHandler;
    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap;
    private ImageDownloaderListener<T> mImageDowloadListener;

    public interface ImageDownloaderListener<T> {
        void onImageDownloaded(T target, Bitmap bitmap);
    }


    public ImageDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
        mRequestMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void onLooperPrepared() {
        // request handler has to be created here. if created in constructor. then it is associated with main thread,
        // which will result in error because network access in main thread is not allowed.
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case THUMNAIL_DOWNLAOD:
                        T obj = (T) message.obj;
                        Log.i(TAG, "get a message for " + mRequestMap.get(obj));
                        handleImageDownloadRequest(obj);
                        break;
                    default:
                }
            }
        };
    }

    public void setImageDowloadListener(ImageDownloaderListener<T> imageDowloadListener) {
        mImageDowloadListener = imageDowloadListener;
    }

    public void queueImageDownload(T target, String url) {
        if (mRequestMap.get(target) == null) {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(THUMNAIL_DOWNLAOD, target).sendToTarget();
        }
    }

    public void removeFromQueue(T target) {
        mRequestMap.remove(target);
    }

    private void handleImageDownloadRequest(final T target) {
        String url = mRequestMap.get(target);
        if (url == null) {      // the download request has been removed.
            return;
        }
        try {
            byte[] imgBytes = new ImagePicker().fetchUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
            if (bitmap == null) {
                Log.e(TAG, "bitmap for " + url + " cannot be decoded into Bitmap.");
            }

            url = mRequestMap.get(target);
            if (url != null && mImageDowloadListener != null) {
                mRequestMap.remove(target);
                mResponseHandler.post(new Runnable() {
                      @Override
                      public void run() {
                          mImageDowloadListener.onImageDownloaded(target, bitmap);
                      }
                  }
                );
            }

        } catch (IOException e) {
            Log.e(TAG, "handleRequest() failed: " + e);
        }

    }

}
