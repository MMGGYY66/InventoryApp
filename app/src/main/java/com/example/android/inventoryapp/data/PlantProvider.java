package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.inventoryapp.data.PlantContract.PlantEntry;

import static com.example.android.inventoryapp.data.PlantContract.PlantEntry.COLUMN_PLANT_QUANTITY;

/**
 * {@link ContentProvider} for LIF app.
 */

public class PlantProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = PlantProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the plants table
     */
    private static final int PLANTS = 100;

    /**
     * URI matcher code for the content URI for a single plant in the plants table
     */
    private static final int PLANT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.inventoryapp/plants" will map to the
        // integer code {@link #PLANTS}. This URI is used to provide access to MULTIPLE rows
        // of the plants table.
        sUriMatcher.addURI(PlantContract.CONTENT_AUTHORITY, PlantContract.PATH_PLANTS, PLANTS);

        // The content URI of the form "content://com.example.android.inventoryapp/plants/#" will map to the
        // integer code {@link #PLANT_ID}. This URI is used to provide access to ONE single row
        // of the plants table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.inventoryapp/plants/3" matches, but
        // "content://com.example.android.inventoryapp/plants" (without a number at the end) doesn't match.
        sUriMatcher.addURI(PlantContract.CONTENT_AUTHORITY, PlantContract.PATH_PLANTS + "/#", PLANT_ID);
    }

    /**
     * Database helper object
     */
    private PlantDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new PlantDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PLANTS:
                // For the PLANTS code, query the plants table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the plants table.
                cursor = database.query(PlantEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PLANT_ID:
                // For the PLANT_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.inventoryapp/plants/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PlantEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the plants table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PlantEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLANTS:
                return PlantContract.PlantEntry.CONTENT_LIST_TYPE;
            case PLANT_ID:
                return PlantContract.PlantEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLANTS:
                return insertPlant(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a plant into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPlant(Uri uri, ContentValues values) {
        // Check that the plant name is not null
        String plantName = values.getAsString(PlantEntry.COLUMN_PLANT_NAME);
        if (plantName == null || plantName.trim().length() == 0) {
            throw new IllegalArgumentException("Plant requires a name");
        }

        // Check that the plant price is not null or has a negative value
        Double plantPrice = values.getAsDouble(PlantEntry.COLUMN_PLANT_PRICE);
        if (plantPrice == null || plantPrice < 0) {
            throw new IllegalArgumentException("Plant requires a valid price");
        }

        // Check that the plant quantity is not null or has a negative value
        Integer plantQuantity = values.getAsInteger(COLUMN_PLANT_QUANTITY);
        if (plantQuantity == null || plantQuantity < 0) {
            throw new IllegalArgumentException("Plant requires a valid quantity");
        }

        // Check that the name of the supplier is not null
        String supplierName = values.getAsString(PlantEntry.COLUMN_PLANT_SUPPLIER_NAME);
        if (supplierName == null || supplierName.trim().length() == 0) {
            throw new IllegalArgumentException("It's required a valid name for the supplier");
        }

        // Check that the email address of the supplier is not null
        String supplierEmail = values.getAsString(PlantEntry.COLUMN_PLANT_SUPPLIER_EMAIL);
        if (supplierEmail == null || supplierEmail.trim().length() == 0) {
            throw new IllegalArgumentException("It's required a valid email address for the supplier");
        }

        // No need to check the Strings for the plant image and for the supplier's phone
        // for these data are not mandatory (any value is valid, including null).

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new plant with the given values
        long id = database.insert(PlantEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the plant content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLANTS:
                return updatePlant(uri, contentValues, selection, selectionArgs);
            case PLANT_ID:
                // For the PLANT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PlantEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePlant(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update plants in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more plants).
     * Return the number of rows that were successfully updated.
     */
    private int updatePlant(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link PlantEntry#COLUMN_PLANT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(PlantEntry.COLUMN_PLANT_NAME)) {
            String plantName = values.getAsString(PlantEntry.COLUMN_PLANT_NAME);
            if (plantName == null || plantName.trim().length() == 0) {
                throw new IllegalArgumentException("Plant requires a name");
            }
        }

        // If the {@link PlantEntry#COLUMN_PLANT_PRICE} key is present,
        // check that the plant price is not null or has a negative value
        if (values.containsKey(PlantEntry.COLUMN_PLANT_PRICE)) {
            Double plantPrice = values.getAsDouble(PlantEntry.COLUMN_PLANT_PRICE);
            if (plantPrice == null || plantPrice < 0) {
                throw new IllegalArgumentException("Plant requires a valid price");
            }
        }

        // If the {@link PlantEntry#COLUMN_PLANT_QUANTITY} key is present,
        // Check that the plant quantity is not null or has a negative value
        if (values.containsKey(COLUMN_PLANT_QUANTITY)) {
            Integer plantQuantity = values.getAsInteger(COLUMN_PLANT_QUANTITY);
            if (plantQuantity == null || plantQuantity < 0) {
                throw new IllegalArgumentException("Plant requires a valid quantity");
            }
        }

        // If the {@link PlantEntry#COLUMN_PLANT_SUPPLIER_NAME} key is present,
        // check that the name of the supplier is not null
        if (values.containsKey(PlantEntry.COLUMN_PLANT_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(PlantEntry.COLUMN_PLANT_SUPPLIER_NAME);
            if (supplierName == null || supplierName.trim().length() == 0) {
                throw new IllegalArgumentException("It's required a valid name for the supplier");
            }
        }

        // If the {@link PlantEntry#COLUMN_PLANT_SUPPLIER_EMAIL} key is present
        // check that the email address of the supplier is not null
        if (values.containsKey(PlantEntry.COLUMN_PLANT_SUPPLIER_EMAIL)) {
            String supplierEmail = values.getAsString(PlantEntry.COLUMN_PLANT_SUPPLIER_EMAIL);
            if (supplierEmail == null || supplierEmail.trim().length() == 0) {
                throw new IllegalArgumentException("It's required a valid email address for the supplier");
            }
        }

        // No need to check the Strings for the plant image and for the supplier's phone
        // for these data are not mandatory (any value is valid, including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(PlantEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLANTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(PlantEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PLANT_ID:
                // Delete a single row given by the ID in the URI
                selection = PlantEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(PlantContract.PlantEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }
}