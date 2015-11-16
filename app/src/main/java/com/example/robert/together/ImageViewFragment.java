package com.example.robert.together;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImageViewFragment extends Fragment {
    public static final String ARG_URL = "ARG_URL";
    private ImageView mImageView;
    private String mUrl;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Bundle args = getArguments();
        mUrl = args.getString(ARG_URL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image_view, container, false);
        mImageView = (ImageView) v.findViewById(R.id.fragment_image_view);
        setupImageView();

        return v;
    }

    public static Fragment newFragment(String url) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_URL, url);

        Fragment fragment = new ImageViewFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private void setupImageView() {
        final ImageCache cache = ImageCache.getInstance(getContext());
        Bitmap bitmap = cache.getCache(mUrl);
        if (bitmap != null) {
            mImageView.setImageBitmap(bitmap);
        } else {
            cache.cacheImage(mUrl, new Runnable() {
                @Override
                public void run() {
                    Bitmap bmp = cache.getCache(mUrl);
                    if (bmp != null) {
                        mImageView.setImageBitmap(bmp);
                    } else {
                        mImageView.setImageResource(R.drawable.default_man);
                    }
                }
            });
        }
    }

}
