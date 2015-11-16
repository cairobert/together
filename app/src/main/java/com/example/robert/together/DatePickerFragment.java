package com.example.robert.together;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by robert on 11/4/15.
 */
public class DatePickerFragment extends DialogFragment {
    public static final String EXTRA_DATE = "com.example.robert.together.extra_date";
    private static final String ARG_DATE = "cur_date";

    private DatePicker mDatePicker;

    public static DialogFragment newInstance() {
        return newInstance(new Date());
    }
    public static DialogFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);

        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_date, null);
        mDatePicker = (DatePicker) v.findViewById(R.id.dialog_date_date_picker);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime((Date) getArguments().getSerializable(ARG_DATE));
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        mDatePicker.init(year, month, day, null);


        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int year = mDatePicker.getYear();
                        int month = mDatePicker.getMonth();
                        int day = mDatePicker.getDayOfMonth();
                        sendResult(new GregorianCalendar(year, month, day).getTime(), Activity.RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

//        boolean enableNegative = getArguments().getBoolean(ARG_ENABLE_NEGATIVE, false);

//        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
//        negativeButton.setEnabled(false);

        return dialog;
    }

    private void sendResult(Date date, int resultCode) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATE, date);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
