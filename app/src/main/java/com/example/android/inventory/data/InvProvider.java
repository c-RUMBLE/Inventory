package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventory.data.InvContract.InvEntry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class InvProvider extends ContentProvider {

    public static final String LOG_TAG =InvProvider.class.getName();

    private InvDbHelper mDbHelper;

    private static final int INV = 100;

    private static final int INV_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InvContract.CONTENT_AUTHORITY,InvContract.PATH_INV,INV);
        sUriMatcher.addURI(InvContract.CONTENT_AUTHORITY,InvContract.PATH_INV + "/#",INV_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InvDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database =mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match =sUriMatcher.match(uri);
        switch (match) {
            case INV:
                cursor = database.query(InvEntry.TABLE_NAME,projection,selection,selectionArgs,
                        null,null,sortOrder);
                break;
            case INV_ID:
                selection = InvEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InvEntry.TABLE_NAME,projection,selection,selectionArgs,
                        null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INV:
                return InvEntry.CONTENT_LIST_TYPE;
            case INV_ID:
                return InvEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INV:
                return insertInv(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertInv(Uri uri, ContentValues values) {

            String name = values.getAsString(InvEntry.COLUMN_INV_NAME);
            if(name == null) {
                throw new IllegalArgumentException("Product must be named");
            }

            String phone = values.getAsString(InvEntry.COLUMN_INV_SUP_PHONE);
            if(phone == null || !InvEntry.isValidPhoneNumber(phone)) {
                throw new IllegalArgumentException("Provide valid Phone number");
            }

            String email = values.getAsString(InvEntry.COLUMN_INV_SUP_EMAIL);
            if(email == null || !InvEntry.isValidEmail(email)) {
                throw new IllegalArgumentException("Provide valid Email");
            }

            Integer quantity = values.getAsInteger(InvEntry.COLUMN_INV_QUANTITY);
            if(quantity != null && quantity<0) {
                throw new IllegalArgumentException("Need valid quantity");
            }

            Integer price = values.getAsInteger(InvEntry.COLUMN_INV_PRICE);
            if(price != null && price<0) {
                throw new IllegalArgumentException("Need valid price");
            }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(InvEntry.TABLE_NAME,null,values);

        if(id == -1) {
            Log.e(LOG_TAG,"Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INV:
                rowsDeleted = database.delete(InvEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INV_ID:
                selection = InvEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(InvEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if(rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INV:
                return updateInv(uri, values, selection, selectionArgs);
            case INV_ID:
                selection = InvEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateInv(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateInv(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if(values.containsKey(InvEntry.COLUMN_INV_NAME)) {
            String name = values.getAsString(InvEntry.COLUMN_INV_NAME);
            if(name == null) {
                throw new IllegalArgumentException("Product must be named");
            }
        }

        if(values.containsKey(InvEntry.COLUMN_INV_SUP_PHONE)) {
            String phone = values.getAsString(InvEntry.COLUMN_INV_SUP_PHONE);
            if(phone == null || !InvEntry.isValidPhoneNumber(phone)) {
                throw new IllegalArgumentException("Provide valid Phone number");
            }
        }

        if(values.containsKey(InvEntry.COLUMN_INV_SUP_EMAIL)) {
            String email = values.getAsString(InvEntry.COLUMN_INV_SUP_EMAIL);
            if(email == null || !InvEntry.isValidEmail(email)) {
                throw new IllegalArgumentException("Provide valid Email");
            }
        }

        if(values.containsKey(InvEntry.COLUMN_INV_QUANTITY)) {
            Integer quantity = values.getAsInteger(InvEntry.COLUMN_INV_QUANTITY);
            if(quantity != null && quantity<0) {
                throw new IllegalArgumentException("Need valid quantity");
            }
        }

        if(values.containsKey(InvEntry.COLUMN_INV_PRICE)) {
            Integer price = values.getAsInteger(InvEntry.COLUMN_INV_PRICE);
            if(price != null && price<0) {
                throw new IllegalArgumentException("Need valid price");
            }
        }

        if(values.size()==0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(InvEntry.TABLE_NAME,values,selection,selectionArgs);
        if(rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
