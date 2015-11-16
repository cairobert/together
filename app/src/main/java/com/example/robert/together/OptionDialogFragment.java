package com.example.robert.together;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by robert on 11/12/15.
 */
public class OptionDialogFragment extends DialogFragment {
    public static final String EXTRA_CHOSEN_ID =
            "com.example.robert.together.option_fragment.extra_chosen_id";

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_OPTIONS = "arg_options";

    public static DialogFragment newInstance(ArrayList<String> options) {
        return newInstance(null, options);
    }

    public static DialogFragment newInstance(String title, ArrayList<String> options) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putStringArrayList(ARG_OPTIONS, options);

        DialogFragment fragment = new OptionDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ArrayList<String> options = getArguments().getStringArrayList(ARG_OPTIONS);
        String title = getArguments().getString(ARG_TITLE);


        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
                .setItems(options.toArray(new String[]{}), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK, which);
                    }
                });
        if (title != null && title.isEmpty()) {
            builder.setTitle(title);
        }

        return builder.create();
    }

    private void sendResult(int resultCode, int idChosen) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CHOSEN_ID, idChosen);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
        dismiss();
    }



}
