package com.example.android.inventoryapp;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.PlantContract.PlantEntry;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * Allows user to show all the details about a db saved plant.
 * In this activity user can with a button increase/decrease plant's
 * stock quantity, place orders to suppliers by phone or email, or
 * delete/edit all plant's attributes through the related MenuItems.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /**
     * Activity context
     */
    final Context mContext = this;

    /**
     * Identifier for the plant data loader
     */
    private static final int PLANT_LOADER = 1;

    /**
     * TextView field for the plant's name
     */
    private TextView mNameTextView;

    /**
     * TextView field for the plant's price
     */
    private TextView mPriceTextView;

    /**
     * TextView field for the plant's stock quantity
     */
    private TextView mQuantityTextView;

    /**
     * TextView field for the plant's supplier name
     */
    private TextView mSupplierNameTextView;

    /**
     * TextView field for the plant's supplier phone
     */
    private TextView mSupplierPhoneTextView;

    /**
     * TextView field for the plant's supplier email
     */
    private TextView mSupplierEmailTextView;

    /**
     * ImageButton to place an order to the supplier
     * by phone
     */
    private ImageButton mButtonOrderSupplierByPhone;

    /**
     * ImageButton to place an order to the supplier
     * by email
     */
    private ImageButton mButtonOrderSupplierByEmail;

    /**
     * ImageButton to decrement plant's stock quantity
     * by 1
     */
    private ImageButton mButtonDecrementPlantQuantity;

    /**
     * ImageButton to increment plant's stock quantity
     * by 1
     */
    private ImageButton mButtonIncrementPlantQuantity;

    /**
     * ImageView for the plant's image
     */
    private ImageView mImagePlant;

    /**
     * Content URI for the existing plant
     */
    private Uri mCurrentPlantUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Find all relevant views that we will need to show all the plant's
        // attributes and make actions on them
        mNameTextView = (TextView) findViewById(R.id.detail_plant_name);
        mPriceTextView = (TextView) findViewById(R.id.detail_plant_price);
        mQuantityTextView = (TextView) findViewById(R.id.detail_plant_stock_quantity);
        mSupplierNameTextView = (TextView) findViewById(R.id.detail_supplier_name);
        mSupplierPhoneTextView = (TextView) findViewById(R.id.detail_supplier_phone);
        mSupplierEmailTextView = (TextView) findViewById(R.id.detail_supplier_email);
        mButtonOrderSupplierByEmail = (ImageButton) findViewById(R.id.detail_button_supplier_email);
        mButtonOrderSupplierByPhone = (ImageButton) findViewById(R.id.detail_button_supplier_phone);
        mButtonIncrementPlantQuantity = (ImageButton) findViewById(R.id.detail_button_stock_increment);
        mButtonDecrementPlantQuantity = (ImageButton) findViewById(R.id.detail_button_stock_decrement);
        mImagePlant = (ImageView) findViewById(R.id.detail_plant_image_view);


        Intent intent = getIntent();
        mCurrentPlantUri = intent.getData();
        if(mCurrentPlantUri != null) {
            // Initialize a loader to read the plant data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(PLANT_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,                       // Parent activity context
                mCurrentPlantUri,           // Table to query
                null,                       // Projection
                null,                       // Selection clause
                null,                       // Selection arguments
                null                        // Default sort order
        );
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if(cursor.moveToFirst()) {

            // Find the columns of plant attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_IMAGE);
            int supplierNameColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_SUPPLIER_PHONE);
            int supplierEmailColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_SUPPLIER_EMAIL);

            // Extract out the value from the Cursor for the respective column index
            final String name = cursor.getString(nameColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            final int quantity = cursor.getInt(quantityColumnIndex);
            final String image = cursor.getString(imageColumnIndex);
            final String supplierName = cursor.getString(supplierNameColumnIndex);
            final String supplierPhone = cursor.getString(supplierPhoneColumnIndex);
            final String supplierEmail = cursor.getString(supplierEmailColumnIndex);

            // Update the views on the screen with the values from the database
            mNameTextView.setText(name);
            mPriceTextView.setText(String.format("%.02f", price));
            mQuantityTextView.setText(String.valueOf(quantity));
            mSupplierNameTextView.setText(supplierName);
            mSupplierPhoneTextView.setText(supplierPhone);
            mSupplierEmailTextView.setText(supplierEmail);


            // Set saved image for the current plant and transform it
            // to a circle shape, based on the image placeholder
            Picasso.with(this).load(image)
                    .placeholder(R.drawable.ic_plant_image_placeholder)
                    .transform(new CropCircleTransformation())
                    .fit()
                    .centerCrop()
                    .into(mImagePlant);


            // Set OnClickListener on plant's stock quantity decrement button
            mButtonDecrementPlantQuantity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Reduce plant quantity, checking before if updated quantity is more than 0.
                    if (quantity > 0) {
                        int updatedPlantQuantity = quantity - 1;
                        ContentValues values = new ContentValues();
                        values.put(PlantEntry.COLUMN_PLANT_QUANTITY, updatedPlantQuantity);
                        mContext.getContentResolver().update(mCurrentPlantUri, values, null, null);
                    } else {
                        // In the case quantity is equal to zero, send a Toast message to user
                        // advising the plant is out of stock
                        Toast.makeText(mContext, mContext.getString(R.string.error_button_decrement_plant_quantity),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Set OnClickListener on plant's stock quantity increment button
            mButtonIncrementPlantQuantity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Increase plant quantity, checking before if updated quantity is less than 9999 pcs.
                    if (quantity < 9999) {
                        int updatedPlantQuantity = quantity + 1;
                        ContentValues values = new ContentValues();
                        values.put(PlantEntry.COLUMN_PLANT_QUANTITY, updatedPlantQuantity);
                        mContext.getContentResolver().update(mCurrentPlantUri, values, null, null);
                    } else {
                        // In the case quantity is equal to 9999 pcs (stock quantity limit), send a Toast message to user
                        // advising the plant stock is full.
                        Toast.makeText(mContext, mContext.getString(R.string.error_button_increment_plant_quantity),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Set OnClickListener on mButtonOrderSupplierByEmail. In this way user can
            // place an order by email, choosing to send it only via email clients.
            // The email address of the supplier, the subject and text of the email order
            // are yet compiled. User has only to put the desired stock quantity to order.
            mButtonOrderSupplierByEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:" + supplierEmail));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString((R.string.order_email_subject), name));
                    emailIntent.putExtra(Intent.EXTRA_TEXT, getString((R.string.order_email_object), supplierName, name));
                    if (emailIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(Intent.createChooser(emailIntent, getString(R.string.label_intent_order_by_email)));
                    }
                }
            });

            // Set OnClickListener on mButtonOrderSupplierByPhone to make a call
            // to the supplier for an order
            mButtonOrderSupplierByPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                    phoneIntent.setData(Uri.parse("tel:" + supplierPhone));
                    if (phoneIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(phoneIntent);
                    }
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Loader can't be invalidated, so there's to action to do.
    }

    // Inflate the menu options from the res/menu/menu_detail.xml file.
    // This adds menu items to the app bar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mCurrentPlantUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Edit Plant" menu option
            case R.id.action_edit:
                editPlant();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to launch EditorActivity with the current plant URI so the item can be edited
     */
    public void editPlant() {
        Intent intent = new Intent(DetailActivity.this, EditorActivity.class);
        intent.setData(mCurrentPlantUri);
        startActivity(intent);
    }

    /**
     * Method to ask confirmation to the user for deleting the current plant
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the plant.
                deletePlant();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the plant.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the plant in the database.
     */
    private void deletePlant() {
        // Only perform the delete if this is an existing plant.
        if (mCurrentPlantUri != null) {
            // Call the ContentResolver to delete the plant at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPlantUri
            // content URI already identifies the plant that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentPlantUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_plant_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_plant_successful),
                        Toast.LENGTH_SHORT).show();
            }

            // Close the activity
            finish();
        }
    }
}
