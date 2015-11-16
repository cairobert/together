package com.example.robert.together;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class ProfileActivity extends SingleFragmentActivity {
    private static final String PERSON_ID = "PERSON_ID";
    private static final String TAG = "ProfileActivity";

    @Override
    protected Fragment createFragment() {
        return ProfileFragment.newInstance(getIntent().getIntExtra(PERSON_ID, 0));
    }

    public static Intent newIntent(Context ctx, int personId) {
        Intent intent = new Intent(ctx, ProfileActivity.class);
        intent.putExtra(PERSON_ID, personId);

        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected()");
        if (item.getItemId() == android.R.id.home) {
            Log.i(TAG, "android.R.id.home");
        }
        return super.onOptionsItemSelected(item);
    }
}
