package com.example.robert.together;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by robert on 11/10/15.
 */
public class SimpleImageAdapter extends RecyclerView.Adapter<SimpleImageAdapter.ImageHolder> {
    private static final String TAG = "SimpleImageAdapter";

    private List<String> mPictures;
    private OnItemClickListener mListener;
    private Context mContext;

    public SimpleImageAdapter(Context ctx, List<String> pictures, OnItemClickListener listener) {
        mPictures = pictures;
        mListener = listener;
        mContext = ctx;
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_image, parent, false);
        ImageHolder holder = new ImageHolder(v, mListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(ImageHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder, position: " + position + ", url: " + mPictures.get(position));
        holder.bindUrl(mPictures.get(position));
    }

    @Override
    public int getItemCount() {
        return mPictures.size();
    }

    public List<String> getPictures() {
        return mPictures;
    }

    public static class ImageHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private String mUrl;
        private ImageView mImageView;
        private OnItemClickListener mClickListener;
        private ImageCache mImageCache;

        public ImageHolder(View itemView, OnItemClickListener listener) {
            super(itemView);

            mImageView = (ImageView) itemView;
            mImageView.setOnClickListener(this);
            mClickListener = listener;
            mImageCache = ImageCache.getInstance(TogetherApp.getContext());
        }

        @Override
        public void onClick(View v) {
            mClickListener.itemClicked(this, getAdapterPosition());
        }

        public void bindUrl(String url) {
            mUrl = url;
            Bitmap bitmap = mImageCache.getCache(mUrl);
            if (bitmap != null) {
                mImageView.setImageBitmap(bitmap);
            } else {
                mImageCache.cacheImage(mUrl, new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bmp = mImageCache.getCache(mUrl);
                        if (bmp != null) {
                            mImageView.setImageBitmap(bmp);
                        }
                    }
                });
            }
        }
    }

    public interface OnItemClickListener {
        void itemClicked(SimpleImageAdapter.ImageHolder holder, int position);
    }
}
