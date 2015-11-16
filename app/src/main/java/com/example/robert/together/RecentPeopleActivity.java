package com.example.robert.together;

import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by robert on 11/3/15.
 */
public class RecentPeopleActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return NearbyPeopleFragment.newFragment();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i("RecentPeopleActivity", "onLowMemory");
    }
}
