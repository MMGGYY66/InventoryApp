package com.example.android.inventoryapp;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.android.inventoryapp.data.PlantContract.PlantEntry;
import static com.example.android.inventoryapp.data.PlantContract.PlantEntry.COLUMN_PLANT_QUANTITY;

/**
 * {@link PlantCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of plant data as its data source. This adapter knows
 * how to create list items for each row of plant data in the {@link Cursor}.
 */

public class PlantCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link PlantCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public PlantCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the plant data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current plant can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @SuppressLint("DefaultLocale")
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list tem layout
        TextView namePlantTextView = (TextView) view.findViewById(R.id.plant_name);
        TextView pricePlantTextView = (TextView) view.findViewById(R.id.plant_price);
        TextView quantityPlantTextView = (TextView) view.findViewById(R.id.plant_quantity);
        ImageButton saleButtonListView = (ImageButton) view.findViewById(R.id.list_view_decrement_plant_quantity);

        // Find the columns of the plant attributes that we're interested in and
        // read the attributes from the Cursor of the current plant
        String plantName = cursor.getString(cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_NAME));
        Double plantPrice = cursor.getDouble(cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_PRICE));
        final Integer plantQuantity = cursor.getInt(cursor.getColumnIndex(COLUMN_PLANT_QUANTITY));
        final Uri plantUri = ContentUris.withAppendedId(PlantEntry.CONTENT_URI,
                cursor.getInt(cursor.getColumnIndexOrThrow(PlantEntry._ID)));

        // Update the TextViews with the attributes of the current plant
        namePlantTextView.setText(plantName);
        pricePlantTextView.setText(String.format("%.02f", plantPrice));
        quantityPlantTextView.setText(String.valueOf(plantQuantity));

        // Implement onClickListener method to ImageButton to reduce plant quantity
        // by one every time button is touched on the ListView row
        saleButtonListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reduce plant quantity, checking before if updated quantity is more than 0.
                if (plantQuantity > 0) {
                    int updatedPlantQuantity = plantQuantity - 1;
                    ContentValues values = new ContentValues();
                    values.put(PlantEntry.COLUMN_PLANT_QUANTITY, updatedPlantQuantity);
                    context.getContentResolver().update(plantUri, values, null, null);
                } else {
                    // In the case quantity is equal to zero, send a Toast message to user
                    // advising the plant is out of stock
                    Toast.makeText(context, context.getString(R.string.error_button_decrement_plant_quantity),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
