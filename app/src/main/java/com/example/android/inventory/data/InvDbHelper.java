package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class InvDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "inventory.db";

    public InvDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String SQL_CREATE_ENTRIES =  "CREATE TABLE " + InvContract.InvEntry.TABLE_NAME + " ("
            + InvContract.InvEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + InvContract.InvEntry.COLUMN_INV_NAME + " TEXT NOT NULL, "
            + InvContract.InvEntry.COLUMN_INV_IMAGE + " BLOB, "
            + InvContract.InvEntry.COLUMN_INV_PRICE + " REAL NOT NULL, "
            + InvContract.InvEntry.COLUMN_INV_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
            + InvContract.InvEntry.COLUMN_INV_SUP_PHONE + " TEXT NOT NULL, "
            + InvContract.InvEntry.COLUMN_INV_SUP_EMAIL + " TEXT NOT NULL);";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + InvContract.InvEntry.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
