package com.example.robert.together;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by robert on 11/10/15.
 */
public class EditTextFragment extends Fragment {
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_CONTENT = "arg_content";

    private String mTitle;
    private String mContent;
    private TextView mContentTextView;

    public static Fragment newInstance(String title, String content) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CONTENT, content);

        EditTextFragment fragment = new EditTextFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mTitle = args.getString(ARG_TITLE, "");
        mContent = args.getString(ARG_CONTENT, "");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_text, container, false);
        mContentTextView = (TextView) v.findViewById(R.id.fragment_edit_text_content_text_view);
        mContentTextView.setText(mContent);

        return v;
    }

}
