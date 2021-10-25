package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InvContract.InvEntry;

public class InvCursorAdapter extends CursorAdapter {

    String LOG_TAG = InvCursorAdapter.class.getName();
    public InvCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_view,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameView = (TextView) view.findViewById(R.id.name_product);
        TextView priceView = (TextView) view.findViewById(R.id.price_product);
        TextView quantityView = (TextView) view.findViewById(R.id.quantity_product);

        String productName = cursor.getString(cursor.getColumnIndexOrThrow(InvEntry.COLUMN_INV_NAME));
        String productPrice = cursor.getString(cursor.getColumnIndexOrThrow(InvEntry.COLUMN_INV_PRICE));
        String productQuantity = cursor.getString(cursor.getColumnIndexOrThrow(InvEntry.COLUMN_INV_QUANTITY));

        final int[] quantity = {Integer.parseInt(productQuantity)};

        nameView.setText(productName);
        priceView.setText(productPrice);
        quantityView.setText(productQuantity);

        Button saleButton = (Button) view.findViewById(R.id.button_sale);
        saleButton.setTag(cursor.getLong(cursor.getColumnIndex(InvEntry._ID)));

        saleButton.setOnClickListener(v -> {
            if(quantity[0]>0) {
                long item_id = (Long) v.getTag();
                quantity[0]--;

                ContentValues values = new ContentValues();
                values.put(InvEntry.COLUMN_INV_QUANTITY, quantity[0]);

                Uri productUri = ContentUris.withAppendedId(InvEntry.CONTENT_URI, item_id);
                int rowUpdated = context.getContentResolver().update(productUri, values, null, null);
                if (rowUpdated == 0) {
                    Toast.makeText(context, R.string.editor_product_update_failure,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.editor_product_update_successful, Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(context, "Quantity cannot be negative", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
