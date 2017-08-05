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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.PlantContract.PlantEntry;
import com.squareup.picasso.Picasso;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * Allows user to create a new plant or edit an existing one.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the plant data loader
     */
    private static final int EXISTING_PLANT_LOADER = 0;

    /**
     * Identifier for the image request code
     */
    private static final int IMAGE_REQUEST_CODE = 0;

    /**
     * String to verify that an image is present and save it
     * even when the activity may be temporarily destroyed
     */
    private static final String STATE_IMAGE_URI = "STATE_IMAGE_URI";

    /**
     * Activity context
     */
    final Context mContext = this;

    /**
     * EditText field to enter the plant's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the plant's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the plant's stock quantity
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the plant's supplier name
     */
    private EditText mSupplierNameEditText;

    /**
     * EditText field to enter the plant's supplier phone
     */
    private EditText mSupplierPhoneEditText;

    /**
     * EditText field to enter the plant's supplier email
     */
    private EditText mSupplierEmailEditText;

    /**
     * Button to add a new plant image or
     * edit one existing
     */
    private Button mButtonAddImage;

    /**
     * ImageView to host the plant's image
     */
    private ImageView mImagePlant;

    /**
     * Content URI for the existing plant (null if it's a new plant)
     */
    private Uri mCurrentPlantUri;

    /**
     * Content URI for the plant's image
     */
    private Uri mImageUri;

    /**
     * Global variable for the plant's image String path
     */
    private String image;

    /**
     * Boolean flag that keeps track of whether the plant has been edited (true) or not (false)
     */
    private boolean mPlantHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPlantHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPlantHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new plant or editing an existing one.
        Intent intent = getIntent();
        mCurrentPlantUri = intent.getData();

        // If the intent DOES NOT contain a plant content URI, then we know that we are
        // creating a new plant.
        if (mCurrentPlantUri == null) {
            // This is a new plant, so change the app bar to say "Add new Plant"
            setTitle(getString(R.string.editor_activity_title_new_plant));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a plant that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing plant, so change app bar to say "Edit Plant"
            setTitle(getString(R.string.editor_activity_title_edit_plant));

            // Initialize a loader to read the plant data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PLANT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_plant_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_plant_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_plant_stock_quantity);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.edit_supplier_phone);
        mSupplierEmailEditText = (EditText) findViewById(R.id.edit_supplier_email);
        mButtonAddImage = (Button) findViewById(R.id.edit_add_plant_image_button);
        mImagePlant = (ImageView) findViewById(R.id.edit_plant_image_view);


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);

        // Setup an InputFilter to mPriceEditText to limit possible characters
        // used by user to insert plant price and its length to the format ####.##
        mPriceEditText.setFilters(new InputFilter[]{new CurrencyFormatInputFilter(6, 2)});

        // Check if mCurrentPlantUri isn't null and set a new LABEL for mButtonAddImage
        // to let the user add a new image or edit the previous chosen plant image
        if (mCurrentPlantUri == null)
        {
            mButtonAddImage.setText(R.string.edit_text_view_add_plant_image);
            mButtonAddImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonAddImagePlant();
                }
            });
        } else {

            mButtonAddImage.setText(R.string.edit_text_view_edit_plant_image);
            mButtonAddImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonAddImagePlant();
                }
            });
        }
    }


    /**
     * Method to limit plant price input to ####.## and limit valid
     * characters used by user to insert price
     */
    private class CurrencyFormatInputFilter implements InputFilter {

        Pattern mPattern;

        CurrencyFormatInputFilter(int digitsBeforeZero, int digitsAfterZero) {
            mPattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero-1) + "}+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Matcher matcher = mPattern.matcher(dest);
            if (!matcher.matches())
                return "";
            return null;
        }

    }

    // Verify if an mImageUri is present and preserve it
    // even if the activity is temporarily destroyed
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mImageUri != null) {
            outState.putString(STATE_IMAGE_URI, mImageUri.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_IMAGE_URI) &&
                !savedInstanceState.getString(STATE_IMAGE_URI).equals("")) {
            mImageUri = Uri.parse(savedInstanceState.getString(STATE_IMAGE_URI));

            // Preserve chosen image for the plant and transform it
            // to a circle shape, based on the image placeholder
            Picasso.with(mContext).load(mImageUri)
                    .placeholder(R.drawable.ic_plant_image_placeholder)
                    .transform(new CropCircleTransformation())
                    .fit()
                    .centerCrop()
                    .into(mImagePlant);
        }
    }

    /**
     * Method to select a picture from device's media storage
     */
    private void buttonAddImagePlant() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.action_select_picture)), IMAGE_REQUEST_CODE);
    }


    /**
     * Get user input from editor and save/update plant into database.
     */
    private void savePlant() {
        // Read all data from input fields and use trim to eliminate leading
        // or trailing white space. For priceString automatically replace "," char for
        // decimal with "." to handle correctly the double value.
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim().replace(",", ".");
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();
        String supplierEmailString = mSupplierEmailEditText.getText().toString().trim();
        String imagePath;

        // Verify if the plant image has been edited by user. If not, use the previous
        // saved image, retrieved from cursor.
        if (mImageUri != null){
            imagePath= mImageUri.toString();
        }
        else {
            imagePath = image;
        }


        if (mCurrentPlantUri == null && mImageUri == null) {
                Toast.makeText(this, R.string.editor_error_empty_image_field, Toast.LENGTH_SHORT).show();
                return;
            }

        // Check if there are blank fields and alert the user to fill all
        // the required data with a Toast message before saving
        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(supplierNameString) ||
                TextUtils.isEmpty(supplierPhoneString) || TextUtils.isEmpty(supplierEmailString)) {
            Toast.makeText(this, R.string.editor_error_fields_incomplete, Toast.LENGTH_SHORT).show();
            return;
        }

        // Redundant check for invalid characters into quantity field (on some mobile
        // phone user can even put letters in the field) to prevent error in the app
        // when trying to save plant data
        if(!(quantityString.matches("[0-9]+"))) {
            Toast.makeText(this, R.string.editor_error_quantity_field, Toast.LENGTH_SHORT).show();
            return;
        }

        // Redundant check for invalid characters into price field (on some mobile
        // phone user can even put letters in the field) to prevent error in the app
        // when trying to save plant data
        if(!(priceString.matches("^[0-9.,]+$"))) {
            Toast.makeText(this, R.string.editor_error_price_field, Toast.LENGTH_SHORT).show();
            return;
        }


        // Check if this is supposed to be a new plant
        // and check if all the fields in the editor are blank
        if (mCurrentPlantUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierPhoneString) && TextUtils.isEmpty(supplierEmailString)) {
            // Since no fields were modified, we can return early without creating a new plant.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and plant attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(PlantEntry.COLUMN_PLANT_NAME, nameString);
        values.put(PlantEntry.COLUMN_PLANT_PRICE, priceString);
        values.put(PlantEntry.COLUMN_PLANT_QUANTITY, quantityString);
        values.put(PlantEntry.COLUMN_PLANT_IMAGE, imagePath);
        values.put(PlantEntry.COLUMN_PLANT_SUPPLIER_NAME, supplierNameString);
        values.put(PlantEntry.COLUMN_PLANT_SUPPLIER_PHONE, supplierPhoneString);
        values.put(PlantEntry.COLUMN_PLANT_SUPPLIER_EMAIL, supplierEmailString);


        // Determine if this is a new or existing plant by checking if mCurrentPlantUri is null or not
        if (mCurrentPlantUri == null) {
            // This is a NEW plant, so insert a new plant into the provider,
            // returning the content URI for the new plant.
            Uri newUri = getContentResolver().insert(PlantEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_plant_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_plant_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING plant, so update the plant with content URI: mCurrentPlantUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPlantUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentPlantUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_plant_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_plant_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new plant, hide the "Delete" menu item.
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
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save plant to database
                savePlant();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the plant hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPlantHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        // If the plant hasn't changed, continue with handling back button press
        if (!mPlantHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all plant attributes, define a projection that contains
        // all columns from the plant table
        String[] projection = {

                PlantEntry._ID,
                PlantEntry.COLUMN_PLANT_NAME,
                PlantEntry.COLUMN_PLANT_PRICE,
                PlantEntry.COLUMN_PLANT_QUANTITY,
                PlantEntry.COLUMN_PLANT_IMAGE,
                PlantEntry.COLUMN_PLANT_SUPPLIER_NAME,
                PlantEntry.COLUMN_PLANT_SUPPLIER_PHONE,
                PlantEntry.COLUMN_PLANT_SUPPLIER_EMAIL
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentPlantUri,       // Query the content URI for the current plant
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of plant attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_IMAGE);
            int supplierNameColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_SUPPLIER_PHONE);
            int supplierEmailColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_SUPPLIER_EMAIL);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            image = cursor.getString(imageColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(String.format("%.02f", price));
            mQuantityEditText.setText(String.valueOf(quantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneEditText.setText(supplierPhone);
            mSupplierEmailEditText.setText(supplierEmail);

            // Set chosen image for the plant and transform it
            // to a circle shape, based on the image placeholder
            Picasso.with(mContext).load(image)
                    .placeholder(R.drawable.ic_plant_image_placeholder)
                    .transform(new CropCircleTransformation())
                    .fit()
                    .centerCrop()
                    .into(mImagePlant);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneEditText.setText("");
        mSupplierEmailEditText.setText("");
    }

    /**
     * Method to set chosen plant image to ImageView holder if the request
     * is successful. Use Picasso to set the image into the ImageView holder,
     * making a shape transformation on it based onn the image placeholder
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && (resultCode == RESULT_OK)) {
            try {
                mImageUri = data.getData();
                int takeFlags = data.getFlags();
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        getContentResolver().takePersistableUriPermission(mImageUri, takeFlags);
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

                // Set saved image for the current plant and transform it
                // to a circle shape, based on the image placeholder
                Picasso.with(mContext).load(mImageUri)
                        .placeholder(R.drawable.ic_plant_image_placeholder)
                        .transform(new CropCircleTransformation())
                        .fit()
                        .centerCrop()
                        .into(mImagePlant);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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
