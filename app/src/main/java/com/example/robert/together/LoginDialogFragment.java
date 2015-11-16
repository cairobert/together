package com.example.robert.together;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by robert on 11/4/15.
 */
public class LoginDialogFragment extends DialogFragment {
    private static final String ARG_IS_LOGIN = "arg_is_login";
    private static final String TAG = "LoginDialogFragment";
    private static final String TAG_DIALOG_DATE = "TagDialogDate";

    private static final int REQ_DATE_PICKER = 1;
    private boolean mIsLogin;
    private boolean mIsMan;
    private EditText mUserNameEditText;
    private EditText mPasswordNameEditText;
    private TextView mBirthdayTextView;
    private RadioButton mManRadioButton;
    private RadioButton mWomanRadioButton;

    private Date mBirthday;

    public static DialogFragment newInstance(boolean isLogin) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ARG_IS_LOGIN, isLogin);

        LoginDialogFragment fragment = new LoginDialogFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mIsLogin = getArguments().getBoolean(ARG_IS_LOGIN);
        mIsMan = true;

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.fragment_dialog_register_or_login, null);
        mUserNameEditText = (EditText) v.findViewById(R.id.fragment_dialog_login_name_text_edit);
        mPasswordNameEditText = (EditText) v.findViewById(R.id.fragment_dialog_login_password_text_edit);

        RadioGroup radioGroup = (RadioGroup) v.findViewById(R.id.fragment_dialog_gender_radio_group);
        mBirthdayTextView = (TextView) v.findViewById(R.id.fragment_dialog_birthday_text_view);

        String title;

        if (!mIsLogin) {        // register
            title = getString(R.string.title_register);
            View.OnClickListener radioButtonListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getId() == R.id.fragment_dialog_man_radio_button) {
                        mIsMan = true;
                    } else {
                        mIsMan = false;
                    }
                }
            };

            mManRadioButton = (RadioButton) v.findViewById(R.id.fragment_dialog_man_radio_button);
            mManRadioButton.setOnClickListener(radioButtonListener);

            mWomanRadioButton = (RadioButton) v.findViewById(R.id.fragment_dialog_woman_radio_button);
            mWomanRadioButton.setOnClickListener(radioButtonListener);

            mBirthdayTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "mBirtdayTextView.onClick");
                    DialogFragment dialogFragment = DatePickerFragment.newInstance();
                    dialogFragment.setTargetFragment(LoginDialogFragment.this, REQ_DATE_PICKER);
                    dialogFragment.show(getFragmentManager(), TAG_DIALOG_DATE);
                }
            });
        } else {
            title = getString(R.string.title_login);
            radioGroup.setVisibility(View.GONE);
            mBirthdayTextView.setVisibility(View.GONE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setView(v)
                .setPositiveButton(android.R.string.ok, null)
        .setNegativeButton(android.R.string.cancel, null);

        final AlertDialog dialog =  builder.create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUserNameEditText.getText().toString().trim().isEmpty()
                        || mPasswordNameEditText.getText().toString().trim().isEmpty()) {
                    Toaster.getInstance(getContext()).showToast("请输入有效文字");
                    return;
                }
                if (mIsLogin) {
                    doLogin();
                } else {
                    if (!mManRadioButton.isChecked() && !mWomanRadioButton.isChecked()) {
                        Toaster.getInstance(getContext()).showToast("请选择性别");
                        return;
                    }
                    if (mBirthdayTextView.getText().toString().trim().isEmpty()) {
                        Toaster.getInstance(getContext()).showToast("请选择生日");
                        return;
                    }
                    doRegister();
                }
            }
        });
        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode != REQ_DATE_PICKER) {
            return;
        }

        Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
        mBirthday = date;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if (!mIsLogin) {
            mBirthdayTextView.setText(String.format("%d/%d/%d", year, month, day));
        }
    }

    private void doLogin() {
        PersonPicker picker =  PersonPicker.getInstance(getContext());
        final String userName = mUserNameEditText.getText().toString().trim();
        String password = mPasswordNameEditText.getText().toString().trim();
        final String encPassword = md5(password);

        picker.login(userName, encPassword, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "doLogin failed: " + request);
                Log.e(TAG, "error: " + e);
                showToast("登陆失败");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String bodyString = response.body().string();
                Log.i(TAG, "doLogin(), bodyString: " + bodyString);
                try {
                    JSONObject jsonObject = new JSONObject(bodyString);
                    int code = jsonObject.getInt("code");
                    if (code != 0) {
                        Log.e(TAG, "doLogin() failed, code: " + code);
                        Log.e(TAG, "msg: " + jsonObject.getString("msg"));
                        showToast("登陆失败");
                    } else {
                        int id = jsonObject.getInt("id");
                        PreferenceManager.getDefaultSharedPreferences(getContext())
                                .edit()
                                .putInt(MainActivity.PREF_LOGIN_ID, id)
                                .commit();
                        launchMainActivity();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "doLogin() json failed: " + e);
                }
            }
        });

    }

    private void showToast(final String msg) {
        Toaster.getInstance(getContext()).showToast(msg);
    }

    private void doRegister() {
        PersonPicker picker =  PersonPicker.getInstance(getContext());
        final String userName = mUserNameEditText.getText().toString().trim();
        String password = mPasswordNameEditText.getText().toString().trim();
        final String encPassword = md5(password);

        picker.register(userName, encPassword, mIsMan, mBirthday.getTime(), new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "doRegister failed: " + request);
                showToast("注册失败");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String bodyString = response.body().string();
                Log.i(TAG, "bodyString: " + bodyString);
                try {
                    JSONObject jsonObject = new JSONObject(bodyString);
                    int code = jsonObject.getInt("code");
                    if (code != 0) {
                        Log.e(TAG, String.format("register failed, code: %d\n, msg: %s", code, jsonObject.getString("msg")));
                        showToast("注册失败");
                    } else {
                        int id = jsonObject.getInt("id");
                        Log.i(TAG, "注册成功, id: " + id);
                        PreferenceManager.getDefaultSharedPreferences(getContext())
                                .edit()
                                .putInt(MainActivity.PREF_LOGIN_ID, id)
                                .commit();
                        launchMainActivity();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "json failed: " + e);
                }
            }
        });
    }

    private void launchMainActivity() {
        dismiss();
        getActivity().finish();

        Intent intent = MainActivity.newIntent(getActivity());
        startActivity(intent);
    }

    private String md5(String data) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = data.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];

        byte[] md5Bytes = md5.digest(byteArray);

        StringBuffer hexValue = new StringBuffer();

        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }

        return hexValue.toString();
    }
}
