package com.example.robert.together;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by robert on 11/10/15.
 */
public abstract class FragmentWithRoundRectView extends Fragment {
    private static final String TAG = "FragmentWithRoundRect";
    private Handler mUiHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUiHandler = new Handler(Looper.getMainLooper());
    }

    public void drawProfile(final ImageView imageView, final String url) {
        drawProfile(imageView, url, ImageCache.getInstance(getActivity()));
    }

    public void drawProfile(final ImageView imageView, final String url, final ImageCache imageCache) {
        if (!isAdded()) {
            return;
        }

        final int diameter = (int) getResources().getDimension(R.dimen.profile_profile_image_diameter);

        if (url == null) {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.default_man);
            drawRoundImageView(imageView, bmp, diameter);
            return;
        }

        Bitmap bitmap = imageCache.getCache(url);
        Log.i(TAG, "bitmap: " + bitmap);
        if (bitmap != null) {
            Log.i(TAG, "have cache: " + url);
            drawRoundImageView(imageView, bitmap, diameter);
        } else {
            Log.i(TAG, "cache image: " + url);
            // TODO: 10/23/15 set to default before fetch
            imageCache.cacheImage(url, new Runnable() {
                @Override
                public void run() {
                    Bitmap bmp = imageCache.getCache(url);
                    if (bmp != null) {
                        Log.i(TAG, "cache succeeded: " + url);
                        drawRoundImageView(imageView, bmp, diameter);         // this will not cause a stack overflow
                    } else { // failed to get image
                        // TODO: 10/23/15 set to default picture
                        Log.i(TAG, "cache failed: " + url);
                        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.default_man);
                        drawRoundImageView(imageView, bmp, diameter);
                    }
                }
            }, true);
        }
    }

    private void drawRoundImageView(final ImageView imageView, final Bitmap bitmap, final int diameter) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) {
                    return;
                }
                Bitmap roundBitmap = ImageDrawer.getRoundBitmap(getContext(), bitmap, diameter);
                Log.i(TAG, "imageView: " + imageView);
                imageView.setImageBitmap(roundBitmap);
            }
        });

    }

}
