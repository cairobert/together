package com.example.robert.together.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by robert on 10/30/15.
 */
public class PersonBaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "person_base.db";
    private static final int VERSION = 2;
    private static final String TAG = "PersonBaseHelper";

    public PersonBaseHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
