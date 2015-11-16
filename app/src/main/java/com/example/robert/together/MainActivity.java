package com.example.robert.together;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

/**
 * Created by robert on 10/11/15.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String PREF_LOGIN_ID = "pref_login_id";
    public static final String PREF_LOGIN_NAME = "pref_login_name";
    public static final String PREF_LOGIN_PASSWORD = "pref_login_password";
    public static final String PREF_LOCATION_LAT = "pref_location_lat";
    public static final String PREF_LOCATION_LON = "pref_location_lon";

    private FragmentTabHost mTabHost;
    private Class[] mFragmentArray = {
            NearbyPeopleFragment.class, // ProfileFragment.class
            SettingsFragment.class,
    };
    private int[] mTextIdArray = {
            R.string.new_meal_text, R.string.personal_info
    };
    private int[] mDrawables = {
            R.drawable.new_meal, R.drawable.settings
    };

    public static Intent newIntent(Context ctx) {
        Intent intent = new Intent(ctx, MainActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Log.i(TAG, "onCreate()");

        setContentView(R.layout.activity_main);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.real_tabcontent);

        int count = mFragmentArray.length;
        for (int i = 0; i < count; i++) {
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(getString(mTextIdArray[i]));
            tabSpec.setIndicator(getTabView(i));
            mTabHost.addTab(tabSpec, mFragmentArray[i], null);
        }
        checkLogin();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "onNewIntent()");
        int flags = intent.getFlags();
        Log.i(TAG, "flags: " + flags);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }

    private View getTabView(int index) {
        View v = getLayoutInflater().inflate(R.layout.tab_item_view, null);
        ImageView img = (ImageView) v.findViewById(R.id.tab_image_view);
        img.setImageDrawable(getResources().getDrawable(mDrawables[index]));

        TextView textView = (TextView) v.findViewById(R.id.tab_text_view);
        textView.setText(getString(mTextIdArray[index]));

        return v;
    }

    private void checkLogin() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int id = pref.getInt(PREF_LOGIN_ID, -1);
        if (id == -1) {
            Intent intent = LoginActivity.newIntent(this);
            startActivity(intent);
            finish();
        } else {
            PersonPool.getInstance(getApplicationContext()).getSelf().setId(id);
        }
    }
}
