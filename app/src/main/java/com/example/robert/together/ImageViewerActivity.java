package com.example.robert.together;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ImageViewerActivity extends AppCompatActivity {
    private static final String ARG_URLS = "ARG_URLS";
    private static final String ARG_INDEX = "ARG_INDEX";
    private ViewPager mViewPager;
    private String[] mUrls;
    private TextView mHintTextView;
    private int mIndex;

    protected Fragment createFragment(String url) {
        return ImageViewFragment.newFragment(url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUrls = getIntent().getStringArrayExtra(ARG_URLS);
        mIndex = getIntent().getIntExtra(ARG_INDEX, 0);

        setContentView(R.layout.activity_image_viewer);
        mViewPager = (ViewPager) findViewById(R.id.activity_image_viewer_viewpager);

        mViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return createFragment(mUrls[position]);
            }

            @Override
            public int getCount() {
                return mUrls.length;
            }
        });
        mViewPager.setCurrentItem(mIndex);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateHint(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setupActionBar();
    }


    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.image_viewer_menu, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        actionBar.setCustomView(v, params);

        mHintTextView = (TextView) v.findViewById(R.id.image_view_menu_title_text_view);
        updateHint(0);
    }

    private void updateHint(int position) {
        if (mUrls.length <= 1) {
            return;
        }
        mHintTextView.setText("" + (position + 1) + "/" + mUrls.length);
    }

    public static Intent newIntent(Context ctx, String[] urls, int idx) {
        Intent intent = new Intent(ctx, ImageViewerActivity.class);
        intent.putExtra(ARG_URLS, urls);
        intent.putExtra(ARG_INDEX, idx);
        return intent;
    }

    public static Intent newIntent(Context ctx, String url) {
        return newIntent(ctx, new String[]{url}, 0);
    }
}
