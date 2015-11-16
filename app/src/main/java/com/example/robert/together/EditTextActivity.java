package com.example.robert.together;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by robert on 11/10/15.
 */
public class EditTextActivity extends Activity {
    public static final String EXTRA_TITLE = "com.example.robert.together.edit_text_activity.extra_title";
    public static final String EXTRA_CONTENT = "com.example.robert.together.edit_text_activity.extra_content";
    private static final String TAG = "EditTextActivity";

    private TextView mTitleTextView;
    private String mTitle;
    private EditText mContentView;
    private TextView mContent;
    private TextView mConfirmTextView;

    public static Intent newIntent(Context ctx, String title, String content) {
        Intent intent = new Intent(ctx, EditTextActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_CONTENT, content);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_edit_text);

        mContentView = (EditText) findViewById(R.id.fragment_edit_text_content_text_view);
        mContentView.setText(getIntent().getStringExtra(EXTRA_CONTENT));

//        Toolbar toolbar = (Toolbar) findViewById(R.id.fragment_edit_text_toolbar);
//        setSupportActionBar(toolbar);



//        setupActionBar();
    }

    private void setupToolBar() {

    }



//    private void setupActionBar() {
//        ActionBar bar = getSupportActionBar();
//        bar.setDisplayHomeAsUpEnabled(true);
//        bar.setDisplayShowTitleEnabled(false);
//        bar.setDisplayShowCustomEnabled(true);
//
//        View v = getLayoutInflater().inflate(R.layout.toolbar_edit_activity, null);
//
//        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
//        bar.setCustomView(v, params);
//
//        mTitleTextView = (TextView) v.findViewById(R.id.toolbar_edit_activity_title_text_view);
//        mTitleTextView.setText(getIntent().getStringExtra(EXTRA_TITLE));
//        mTitle = getIntent().getStringExtra(EXTRA_TITLE);
//
//        mConfirmTextView = (TextView) v.findViewById(R.id.toolbar_edit_activity_ok_text_view);
//        mConfirmTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                exitActivity(Activity.RESULT_OK, mContentView.getText().toString());
//            }
//        });
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                exitActivity(Activity.RESULT_CANCELED, null);
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void exitActivity(int resultCode, String content) {
        Log.i(TAG, "exitActivit, resultCode: " + resultCode);
    }
}
