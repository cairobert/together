package com.example.robert.together;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by robert on 11/3/15.
 */
public class LoginActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return LoginFragment.newInstance();
    }

    public static Intent newIntent(Context ctx) {
        Intent intent = new Intent(ctx, LoginActivity.class);
        return intent;
    }

}
