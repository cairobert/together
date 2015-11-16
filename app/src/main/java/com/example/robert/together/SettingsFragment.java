package com.example.robert.together;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by robert on 11/7/15.
 */
public class SettingsFragment extends FragmentWithRoundRectView {
    private static final String TAG = "SettingsFragment";
    private ImageView mProfileRoundImageView;
    private TextView mNameTextView;
    private ViewGroup mProfileLayout;
    private ViewGroup mSettingsLayout;

    public static Fragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        mProfileLayout = (RelativeLayout) v.findViewById(R.id.fragment_settings_profile_relative_layout);
        mProfileRoundImageView = (ImageView) v.findViewById(R.id.fragment_settings_profile_image_view);
        mNameTextView = (TextView) v.findViewById(R.id.fragment_settings_name_text_view);

        mProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = EditProfileActivity.newIntent(getActivity());
                startActivity(intent);
            }
        });

        mSettingsLayout = (RelativeLayout) v.findViewById(R.id.fragment_settings_settings_relative_layout);
        mSettingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toaster.getInstance(getActivity()).showToast("暂时还没哦");
            }
        });

        PersonPicker picker = PersonPicker.getInstance(getActivity());

        picker.fetchSelf(new Runnable() {
            @Override
            public void run() {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "SettingsFragment fetchSelf");
                        Person p = PersonPool.getInstance(getActivity()).getSelf();
                        mNameTextView.setText(p.getName());
                        drawProfile(mProfileRoundImageView, p.getProfileUrl());
                    }
                });
            }
        });



        return v;
    }
}
