package com.example.robert.together;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by robert on 11/9/15.
 */
public class EditProfileActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return EditProfileFragment.newInstance();
    }

    public static Intent newIntent(Context ctx) {
        return new Intent(ctx, EditProfileActivity.class);
    }



}
