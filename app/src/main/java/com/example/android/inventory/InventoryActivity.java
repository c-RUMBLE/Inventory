/**TODO:
 *  input validation needed,
 *  image function,
*/

package com.example.android.inventory;

import androidx.appcompat.app.AppCompatActivity;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.android.inventory.data.InvContract.InvEntry;

public class InventoryActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final int INV_LOADER = 0;

    InvCursorAdapter mCursorAdapter;

    String LOG_TAG = InventoryActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_product_view);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(InventoryActivity.this, DetailsActivity.class);
            if (intent.resolveActivity(getPackageManager()) != null)
                startActivity(intent);
        });

        ListView invListView = (ListView) findViewById(R.id.inventory_list_view);

        View emptyView = findViewById(R.id.empty_view);
        invListView.setEmptyView(emptyView);

        mCursorAdapter = new InvCursorAdapter(this, null);
        invListView.setAdapter(mCursorAdapter);

        invListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(InventoryActivity.this, DetailsActivity.class);
            Uri currentProductUri = ContentUris.withAppendedId(InvEntry.CONTENT_URI, id);
            Log.e(LOG_TAG,"click:" + id + " " + position);
            intent.setData(currentProductUri);
            if (intent.resolveActivity(getPackageManager()) != null)
                startActivity(intent);
        });

        getLoaderManager().initLoader(INV_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InvEntry._ID,
                InvEntry.COLUMN_INV_NAME,
                InvEntry.COLUMN_INV_PRICE,
                InvEntry.COLUMN_INV_QUANTITY };

        return new CursorLoader(this,
                InvEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}