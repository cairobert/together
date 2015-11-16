package com.example.robert.together;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by robert on 10/23/15.
 */
public class ImageCache {
    private static ImageCache sImageCache;

    private static final String TAG = "ImageCache";
    public static final String IMAGE_DIR = "IMAGE";
    private Map<String, SoftReference<Bitmap>> mBitmapCache;
    private Context mContext;
    private Handler mMainThreadHandler;


    public static ImageCache getInstance(Context ctx) {
        if (sImageCache == null) {
            sImageCache = new ImageCache(ctx);
        }
        return sImageCache;
    }

    private ImageCache(Context ctx) {
        mContext = ctx;
        mMainThreadHandler = new Handler(ctx.getMainLooper());
        mBitmapCache = Collections.synchronizedMap(new WeakHashMap<String, SoftReference<Bitmap>>());
    }

    // should pass application context
    public String getCachePath(String url) {
        int ix = url.lastIndexOf('/');
//        int dotIx = url.lastIndexOf('.');
//        String name = url.substring(ix + 1, dotIx);
//        int hashCode = name.hashCode();
        String suf = ".jpg";

        return  getCacheImageDir() + url.substring(ix + 1);
    }

    public String getCacheImageDir() {
        return mContext.getExternalCacheDir() + File.separator + IMAGE_DIR + File.separator;
    }

    // return paths of srcPath cached in local file system
    public String cacheLocalFile(String srcPath) {
//        int ix = srcPath.lastIndexOf('/');
//        String fname = srcPath.substring(ix + 1);
//        String newName = getCacheImageDir() + fname;
        if (srcPath == null) {
            return  null;
        }
        String cacheName = getCachePath(srcPath);
        boolean ok = Utils.copyFile(srcPath, cacheName);

        return ok? cacheName: null;
    }


    // means if in url or not. and should only cache the last part of url.
    public Bitmap getCache(String url) {
        if (isEmpty(url)) {
            return null;
        }

        SoftReference<Bitmap> bitmapWeakReference = sImageCache.mBitmapCache.get(url);
        Log.i(TAG, "getCache, url: " + url);
        Log.i(TAG, "bitmapReference: " + bitmapWeakReference);
        if (bitmapWeakReference == null) {
            Log.i(TAG, "remove: " + url);
            sImageCache.mBitmapCache.remove(url);
        } else {
            Bitmap bitmap = bitmapWeakReference.get();
            Log.i(TAG, "bitmap: " + (bitmap == null ? "Null":"not null"));
            if (bitmap != null) {
                return bitmap;
            }
        }

        Bitmap bitmap = null;
        String filename = getCachePath(url);
        Log.i(TAG, "getCachePath for url: " + url + ", path: " + filename);
        if (new File(filename).exists()) {
            Log.i(TAG, filename + " exists!");
            File f = new File(filename);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
//            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inTempStorage = new byte[16 * 1024];
            if (f.length() > 1024 * 1024) {
                options.inSampleSize = 4;
            }
            try {
                bitmap = BitmapFactory.decodeStream(new FileInputStream(filename), null, options);
                if (bitmap == null) {
                    Log.e(TAG, "decodeStream returned null");
                } else {
                    sImageCache.mBitmapCache.put(url, new SoftReference<>(bitmap));
                    Log.i(TAG, "decodeStream returned not null");
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "cache image : " + e);
            }
            return bitmap;
        }

        Log.i(TAG, "mBitmapCache, remove: " + url);
        sImageCache.mBitmapCache.remove(url);
        return null;
    }

    public void cacheImage(final String url, final Runnable callback) {
        cacheImage(url, callback, true);
    }

    public void cacheImage(final String url, final Runnable callback, final boolean doOnMainThread) {
        if (isEmpty(url) || getCache(url) != null) {
            if (doOnMainThread) {
                sImageCache.mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.run();
                    }
                });
            } else {
                callback.run();
            }
            return;
        }
        Log.i("ImageCache", "cache url: " + url);

        ThreadManager.getInstance(mContext).addRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bitmap = new ImagePicker().fetchImage(url);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                    File f = new File(getCachePath(url));
                    File parent = f.getParentFile();
                    if (parent.exists() == false) {
                        parent.mkdirs();
                    }

                    FileOutputStream fout = new FileOutputStream(f);
                    fout.write(out.toByteArray());
                    fout.close();
                    sImageCache.mBitmapCache.put(url, new SoftReference<>(bitmap));
                    if (doOnMainThread) {
                        sImageCache.mMainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.run();
                            }
                        });
                    } else {
                        callback.run();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "failed to cache image with url " + url + ", error: " + e);
                }
            }
        });
    }

    public static boolean isEmpty(String s) {
        if (s == null || s.trim().isEmpty()){
            return true;
        }
        return false;
    }

    public void scaleDownImage(File f) {
        return;
//        if (f.length() < 1024 * 1024) {  // < 512K
//            return;
//        }
//        Log.i(TAG, "scaleDownImage, file exists: " + f.exists());
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 4;
//        options.inDither = true;
//        Bitmap bitmap = BitmapFactory.decodeFile(f.getPath(), options);
//
//        try {
//            File tmp = File.createTempFile("tmp", "jpg", new File(getCacheImageDir()));
//            FileOutputStream out = new FileOutputStream(tmp);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
//            out.flush();
//            out.close();
//            Utils.copyFile(tmp.getPath(), f.getPath());
//            scaleDownImage(f);
//        } catch (IOException e) {
//            Log.e(TAG, "scaleDownImage: " + e);
//        }
    }
}
