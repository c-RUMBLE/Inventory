package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.regex.Pattern;

public final class InvContract {
    private InvContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INV = "inv";

    public static final class InvEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INV);
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INV;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INV;

        public static final String TABLE_NAME = "inv";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_INV_NAME = "name";
        public static final String COLUMN_INV_IMAGE = "image";
        public static final String COLUMN_INV_PRICE = "price";
        public static final String COLUMN_INV_QUANTITY = "quantity";
        public static final String COLUMN_INV_SUP_PHONE = "supplier_phone";
        public static final String COLUMN_INV_SUP_EMAIL = "supplier_email";

        static String PHONE_PATTERN = "^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$";
        static String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        public static boolean isValidPhoneNumber(String number) {
            return Pattern.compile(PHONE_PATTERN).matcher(number.trim()).find();
        }

        public static boolean isValidEmail(String email) {
            return Pattern.compile(EMAIL_PATTERN).matcher(email.trim()).matches();
        }
    }
}
