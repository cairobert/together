package com.example.robert.together;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by robert on 11/3/15.
 */
public class LoginFragment extends Fragment {
    private static final String TAG_LOGIN_OR_REGISTER = "TagLoginFragmentOrRegister";
    private Button mLoginButton;
    private Button mRegisterButton;

    public static Fragment newInstance() {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        mLoginButton = (Button) v.findViewById(R.id.fragment_login_login_button);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isLogin = false;
                if (v.getId() == R.id.fragment_login_login_button) {
                    isLogin = true;
                } else {
                    isLogin = false;
                }
                DialogFragment dialogFragment = LoginDialogFragment.newInstance(isLogin);
                dialogFragment.show(getFragmentManager(), TAG_LOGIN_OR_REGISTER);
            }
        };

        mLoginButton.setOnClickListener(listener);

        mRegisterButton = (Button) v.findViewById(R.id.fragment_login_register_button);
        mRegisterButton.setOnClickListener(listener);

        return v;
    }
}
