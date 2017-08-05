package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the LIF app.
 */

public class PlantContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private PlantContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventoryapp/plants/ is a valid path for
     * looking at plants' data. content://com.example.android.inventoryapp/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_PLANTS = "plants";

    /**
     * Inner class that defines constant values for the plants database table.
     * Each entry in the table represents a single plant.
     */
    public static final class PlantEntry implements BaseColumns {

        /** The content URI to access the plant data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PLANTS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of plants.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLANTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single plant.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLANTS;

        /** Name of database table for plants */
        public static final String TABLE_NAME = "plants";

        /**
         * Unique ID number for the plant (only for use in the database table).
         *
         * Type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;

        /**
         * Name of the plant.
         *
         * Type: TEXT
         */
        public static final String COLUMN_PLANT_NAME = "plant_name";

        /**
         * Price of the plant.
         *
         * Type: REAL
         */
        public static final String COLUMN_PLANT_PRICE = "plant_price";

        /**
         * Quantity of the plant in stock.
         *
         * Type: INTEGER
         */
        public static final String COLUMN_PLANT_QUANTITY = "plant_quantity";

        /**
         * Image of the plant.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PLANT_IMAGE ="plant_image";

        /**
         * Supplier Name.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PLANT_SUPPLIER_NAME ="plant_supplier_name";

        /**
         * Supplier Phone.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PLANT_SUPPLIER_PHONE ="plant_supplier_phone";

        /**
         * Supplier Email.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PLANT_SUPPLIER_EMAIL ="plant_supplier_email";

    }
}
