package com.example.android.inventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.data.InvContract.InvEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.FileProvider;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    String LOG_TAG = DetailsActivity.class.getName();

    private static final int EXISTING_PRODUCT_LOADER = 0;

    private Uri mCurrentProductUri;

    private EditText mNameEditText;

    private EditText mPriceEditText;

    private EditText mEmailEditText;

    private EditText mPhoneEditText;

    private EditText mQuantityEditText;

    private FloatingActionButton mIncrementFab;

    private FloatingActionButton mDecrementFab;

    private ImageView mProductImgView;

    private boolean mProductHasChanged = false;

    private String phoneNum = "";

    private String emailId = "";

    private String productName = "";

    static final int REQUEST_IMAGE_GET = 1;

    private Uri fullPhotoUri;

    private Uri outputFileUri;

    String currentPhotoPath;

    private View.OnTouchListener mTouchListener = (view, motionEvent) -> {
        mProductHasChanged = true;
        return false;
    };

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // /storage/emulated/0/Android/data/com.example.android.inventory/files/Pictures
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
Log.e(LOG_TAG,"imag1: "+ image.toString());
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.e(LOG_TAG,"imag2: "+ currentPhotoPath.toString());
        return image;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        fullPhotoUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.ic_baseline_image_24)
                + '/' + getResources().getResourceTypeName(R.drawable.ic_baseline_image_24)
                + '/' + getResources().getResourceEntryName(R.drawable.ic_baseline_image_24) );

        FloatingActionButton del_fab = (FloatingActionButton) findViewById(R.id.delete_fab);
        Button order = (Button) findViewById(R.id.order_button);

        order.setOnClickListener(v -> showOrderConfirmationDialog());

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.detail_activity_title_new_product));
//TODO care of visibility corner cases
            del_fab.setVisibility(View.GONE);
        } else {
            setTitle(getString(R.string.detail_activity_title_product_details));

            del_fab.setVisibility(View.VISIBLE);
            del_fab.setOnClickListener(view -> showDeleteConfirmationDialog());

            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.product_name);
        mPriceEditText = (EditText) findViewById(R.id.product_price);
        mEmailEditText = (EditText) findViewById(R.id.supplier_email);
        mPhoneEditText = (EditText) findViewById(R.id.supplier_phone);
        mQuantityEditText = (EditText) findViewById(R.id.product_quantity);
        mIncrementFab = (FloatingActionButton) findViewById(R.id.increment_fab);
        mDecrementFab = (FloatingActionButton) findViewById(R.id.decrement_fab);
        mProductImgView = (ImageView) findViewById(R.id.product_image);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mIncrementFab.setOnTouchListener(mTouchListener);
        mDecrementFab.setOnTouchListener(mTouchListener);

        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        final boolean isCamera;
                        if (data == null || (data.getData() == null && data.getClipData()==null)) {
                            isCamera = true;
                        } else {
                            final String action = data.getAction();
                            if (action == null) {
                                isCamera = false;
                            } else {
                                isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            }
                        }

                        if (isCamera) {
                            fullPhotoUri = outputFileUri;
                            Log.e(LOG_TAG,"cam");
                        } else {
                            fullPhotoUri = data == null ? null : data.getData();
                            Log.e(LOG_TAG,"file");
                        }

                        Log.e(LOG_TAG,"photu: " + fullPhotoUri);
                        try {
                            Bitmap bm = handleSamplingAndRotationBitmap(this,fullPhotoUri);
                            mProductImgView.setImageBitmap(bm);
                        } catch (IOException e) {
                            Log.e(LOG_TAG,"whyyyyyyyyyy");
                            e.printStackTrace();
                        }
                    }
                });

//2021-10-22 14:49:29.609 29922-29922/com.example.android.inventory E/AndroidRuntime: FATAL EXCEPTION: main
//    Process: com.example.android.inventory, PID: 29922
//    java.lang.OutOfMemoryError: Failed to allocate a 150994952 byte allocation with 25165824 free bytes and 106MB until OOM, max allowed footprint 115086184, growth limit 201326592
//        at java.util.Arrays.copyOf(Arrays.java:3260)
//        at java.lang.AbstractStringBuilder.ensureCapacityInternal(AbstractStringBuilder.java:125)
//        at java.lang.AbstractStringBuilder.append(AbstractStringBuilder.java:686)
//        at java.lang.StringBuilder.append(StringBuilder.java:209)
//        at java.util.Arrays.toString(Arrays.java:4386)
//        at com.example.android.inventory.DetailsActivity.savePet(DetailsActivity.java:485)
//        at com.example.android.inventory.DetailsActivity.lambda$showSaveConfirmationDialog$13$DetailsActivity(DetailsActivity.java:452)
//        at com.example.android.inventory.-$$Lambda$DetailsActivity$QK9zFrEkS2dy0YiPa0g5aoCCfGI.onClick(Unknown Source:2)
//        at com.android.internal.app.AlertController$ButtonHandler.handleMessage(AlertController.java:172)
//        at android.os.Handler.dispatchMessage(Handler.java:106)
//        at android.os.Looper.loop(Looper.java:201)
//        at android.app.ActivityThread.main(ActivityThread.java:6861)
//        at java.lang.reflect.Method.invoke(Native Method)
//        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:547)
//        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:873)
//2021-10-22 14:49:29.611 29922-29922/com.example.android.inventory D/FdInfoManager: MIUI_FD Interested Fd leak events, exceptionClass : java.lang.OutOfMemoryError
//2021-10-22 14:49:29.611 29922-29922/com.example.android.inventory D/AndroidRuntime: interested fdleak event，need raise rlimit temporarily!
//2021-10-22 14:49:29.611 29922-29922/com.example.android.inventory W/AndroidRuntime: finished raiseRlimit, rlim_cur:32768  rlim_max:32768
//2021-10-22 14:49:29.613 29922-29922/com.example.android.inventory D/FdInfoManager: MIUI_FD Interested Fd leak events, exceptionClass : java.lang.OutOfMemoryError
//2021-10-22 14:49:29.620 29922-29922/com.example.android.inventory D/FdInfoManager: MIUI_FD in checkEventAndDumpFd shouldDumpFd : 0
//2021-10-22 14:49:29.628 29922-29922/com.example.android.inventory D/OOMEventManagerFK: checkEventAndDumpheap shouldDumpHeap : 0
//2021-10-22 14:49:29.628 29922-29922/com.example.android.inventory D/RuntimeInjector: checkEventAndDumpheap result : 0

        mProductImgView.setOnClickListener(v -> {

            Intent pickIntent = new Intent();
            pickIntent.setType("image/*");
            pickIntent.setAction(Intent.ACTION_GET_CONTENT);

            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                outputFileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile);
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            else{
                Log.e(LOG_TAG,"photoFile is null");
            }
            String pickTitle = "Select or take a new Picture";
            Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
            chooserIntent.putExtra
                    (
                            Intent.EXTRA_INITIAL_INTENTS,
                            new Intent[] { takePhotoIntent }
                    );
            someActivityResultLauncher.launch(chooserIntent);
        });

    }

    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
/**
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }*/
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        ei = new ExifInterface(input);

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        input.close();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private void orderViaEmail() {
        String editEmail = mEmailEditText.getText().toString().trim();
        String editName = mNameEditText.getText().toString().trim();
        if(editEmail.compareTo(emailId) == 0 && editName.compareTo(productName) == 0) {
            String[] emailAddress = {emailId};
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:"));
            emailIntent.putExtra(Intent.EXTRA_EMAIL,emailAddress);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Placing an order for " + productName);
            if (emailIntent.resolveActivity(getPackageManager()) != null)
                startActivity(emailIntent);
        }
        else if(editEmail.compareTo(emailId) == 0) {
            Toast.makeText(this, "Please save the changed product name first", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Please save the changed email id first", Toast.LENGTH_SHORT).show();
        }
    }

    private void orderViaPhone() {
        String editPhone = mPhoneEditText.getText().toString().trim();
        if(editPhone.compareTo(phoneNum) == 0) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(phoneNum.trim())));
            if (callIntent.resolveActivity(getPackageManager()) != null)
                startActivity(callIntent);
        }
        else {
            Toast.makeText(this, "Please save the changed phone number first", Toast.LENGTH_SHORT).show();
        }
    }

    private void showOrderConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.order_dialog_msg);
        builder.setPositiveButton(R.string.email_order, (dialog, id) -> orderViaEmail());
        builder.setNegativeButton(R.string.phone_order, (dialog, id) -> orderViaPhone());
        builder.setNeutralButton(R.string.cancel, (dialog, id) -> {
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, (dialog, id) -> deletePet());
        builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, (DialogInterface.OnClickListener) (dialog, id) -> {
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePet() {
        if(mCurrentProductUri != null) {
            int rowDeleted = getContentResolver().delete(mCurrentProductUri,null,null);
            if(rowDeleted == 0)
                Toast.makeText(this, getString(R.string.editor_product_deletion_failure), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getString(R.string.editor_product_deletion_successful), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                (dialogInterface, i) -> finish();

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                showSaveConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        (dialogInterface, i) -> NavUtils.navigateUpFromSameTask(DetailsActivity.this);

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSaveConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to save the product?");
        builder.setPositiveButton("Yes", (dialog, id) -> {
            try {
                savePet();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String nameString = mNameEditText.getText().toString().trim();
            if(!TextUtils.isEmpty(nameString)) {
                // Exit activity
                finish();
            }});
        builder.setNegativeButton("No", (dialog, id) -> {
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void savePet() throws IOException {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        byte[] imageInput = new byte[0];

        Log.e(LOG_TAG,"imageInput1 : " + Arrays.toString(imageInput));

        if(fullPhotoUri != null) {
            InputStream iStream = getContentResolver().openInputStream(fullPhotoUri);
            imageInput = getBytes(iStream);
            //Log.e(LOG_TAG,"imageInput2 : " + Arrays.toString(imageInput));
            iStream.close();
        }

        int quantity = 0;
        if(!TextUtils.isEmpty(quantityString))
            quantity = Integer.parseInt(quantityString);

        double price = Double.parseDouble(priceString);

        ContentValues values = new ContentValues();
        values.put(InvEntry.COLUMN_INV_NAME, nameString);
        values.put(InvEntry.COLUMN_INV_IMAGE,imageInput);
        values.put(InvEntry.COLUMN_INV_PRICE, price);
        values.put(InvEntry.COLUMN_INV_QUANTITY, quantity);
        values.put(InvEntry.COLUMN_INV_SUP_PHONE, phoneString);
        values.put(InvEntry.COLUMN_INV_SUP_EMAIL, emailString);


        if(mCurrentProductUri == null) {
            if(TextUtils.isEmpty(nameString) && price == 0.0 && quantity == 0 && TextUtils.isEmpty(phoneString) && TextUtils.isEmpty(emailString)) {
                Toast.makeText(this, getString(R.string.editor_no_inputs),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Uri newUri = getContentResolver().insert(InvEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, R.string.editor_product_insertion_failure,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_product_insertion_successful,
                        Toast.LENGTH_SHORT).show();
            }
        }
        else {
            int rowUpdated = getContentResolver().update(mCurrentProductUri, values,null,null);
            if(rowUpdated == 0) {
                Toast.makeText(this, R.string.editor_product_update_failure,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_product_update_successful, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {

        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if(bitmap != null)
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        else
            Log.e(LOG_TAG,"hmmmmm");
        return stream.toByteArray();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InvEntry._ID,
                InvEntry.COLUMN_INV_NAME,
                InvEntry.COLUMN_INV_IMAGE,
                InvEntry.COLUMN_INV_PRICE,
                InvEntry.COLUMN_INV_QUANTITY,
                InvEntry.COLUMN_INV_SUP_PHONE,
                InvEntry.COLUMN_INV_SUP_EMAIL };

        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_NAME);
            int imageColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_IMAGE);
            int priceColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_QUANTITY);
            int phoneColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_SUP_PHONE);
            int emailColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_SUP_EMAIL);

            String name = cursor.getString(nameColumnIndex);
            byte[] image = cursor.getBlob(imageColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);
            String email = cursor.getString(emailColumnIndex);

            Log.e(LOG_TAG,"image1: "+ Arrays.toString(image) + image.length);

            mNameEditText.setText(name);
            mPriceEditText.setText(Double.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mPhoneEditText.setText(phone);
            mEmailEditText.setText(email);

            if(image != null && image.length != 0) {
                Log.e(LOG_TAG,"image2: "+ Arrays.toString(image));
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                mProductImgView.setImageBitmap(imageBitmap);
            }

            productName += name;
            phoneNum += phone;
            emailId += email;

            mDecrementFab.setOnClickListener(v -> {
                int x = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                if(x>0) {
                    x--;
                    mQuantityEditText.setText(Integer.toString(x));
                } else {
                    Toast.makeText(this, "Quantity cannot be negative", Toast.LENGTH_SHORT).show();
                }
            });

            mIncrementFab.setOnClickListener(v -> {
                int x = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                x++;
                mQuantityEditText.setText(Integer.toString(x));
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mPhoneEditText.setText("");
        mEmailEditText.setText("");
        mProductImgView.setImageResource(R.drawable.ic_baseline_image_24);
    }
}
