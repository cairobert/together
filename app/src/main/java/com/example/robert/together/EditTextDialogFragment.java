package com.example.robert.together;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

/**
 * Created by robert on 11/11/15.
 */
public class EditTextDialogFragment extends DialogFragment {
    public static final String EXTRA_CONTENT = "com.example.robert.together.edit_text_dialog_framgent.extra_content";

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_CONTENT = "arg_content";
    private EditText mContentEditText;

    public static DialogFragment newInstance(String title, String content) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CONTENT, content);

        DialogFragment fragment = new EditTextDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.fragment_dialog_edit_text, null);
        mContentEditText = (EditText) v.findViewById(R.id.fragment_dialog_edit_text_content_edit_text);
        mContentEditText.setText(getArguments().getString(ARG_CONTENT));

        Log.i("EditTextDialogFragment", "title: " + getArguments().getString(ARG_TITLE) + ", content: " + getArguments().getString(ARG_CONTENT));

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getString(ARG_TITLE))
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK, mContentEditText.getText().toString());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }

    private void sendResult(int resultCode, String content) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CONTENT, content);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode ,intent);
    }
}
