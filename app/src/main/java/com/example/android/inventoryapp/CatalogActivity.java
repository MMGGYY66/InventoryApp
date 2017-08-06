package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.PlantContract.PlantEntry;


/**
 * Displays list of plants that were entered and stored in the app.
 */

public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener {

    /**
     * UI elements related with the SearchView
     * on the AppBar
     */
    MenuItem searchMenuItem;
    SearchView searchView;

    /**
     * Query inserted by the user through the AppBar SearchView
     */
    String mSearchQuery = null;


    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    /** Identifier for the plant data loader */
    private static final int PLANT_LOADER = 0;

    /** Adapter for the ListView */
    PlantCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the plant data
        ListView plantListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        plantListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list tem for each row of the plant data in the Cursor.
        // There is no plant data yet (until the loader finishes) so pass in null for the Cursor;
        mCursorAdapter = new PlantCursorAdapter(this, null);
        plantListView.setAdapter(mCursorAdapter);

        // Setup the item click listener to open DetailActivity
        plantListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                // Create new intent to go to {@link DetailActivity}
                Intent intent = new Intent(CatalogActivity.this, DetailActivity.class);

                // Form the content URI that represents the specific list item that was clicked on,
                // by appending the "id" onto the {@link ProductEntry#CONTENT_URI}.
                // Example => content://com.example.android.storeinventory/products/2, for product id = 2
                Uri currentPlantUri = ContentUris.withAppendedId(PlantEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentPlantUri);

                // Launch the {@link DetailActivity} to display the data for the current item
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(PLANT_LOADER, null, this);


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Helper method to delete all plants in the database.
     */
    private void deleteAllPlants() {
        int rowsDeleted = getContentResolver().delete(PlantEntry.CONTENT_URI, null, null);
        Log.v(LOG_TAG, rowsDeleted + " rows deleted from plant database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);

        searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchMenuItem.getActionView();

        // Setting the listener on the SearchView
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to ask confirmation to the user for deleting the current plant
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_plants_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the plant.
                deleteAllPlants();
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle args) {
        String mSelectionArgs;

        // If the search query is not an empty string, query the db with the given mSearchQuery
        if (mSearchQuery != null && !TextUtils.isEmpty(mSearchQuery)) {
            mSelectionArgs = PlantEntry.COLUMN_PLANT_NAME + " LIKE '%" + mSearchQuery + "%'";
            Log.e(LOG_TAG, mSelectionArgs);
        } else {
            // If the search query is an empty string, gets everything from the db
            mSelectionArgs = null;
        }

            // Define a projection that specifies the columns from the table we care about
            String[] projection = {
                    PlantEntry._ID,
                    PlantEntry.COLUMN_PLANT_NAME,
                    PlantEntry.COLUMN_PLANT_PRICE,
                    PlantEntry.COLUMN_PLANT_QUANTITY};

            // Perform a query on the provider using the ContentResolver.
            // Use the {@link PlantEntry#CONTENT_URI} to access the plant data.
            return new CursorLoader(this,    // Parent activity context
                    PlantEntry.CONTENT_URI,  // Parent content URI to query
                    projection,              // The columns to return for each row
                    mSelectionArgs,          // Either null, or the search query the user entered
                    null,                    // Selection criteria
                    null);                   // Default sort order
    }
    

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link PlantCursorAdapter} with this new cursor containing updated plant data
        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(this, "Searching for " + query, Toast.LENGTH_SHORT).show();

        mSearchQuery = searchView.getQuery().toString().trim();

        getLoaderManager().restartLoader(PLANT_LOADER, null, this);
        Log.e(LOG_TAG, "LOADER RESTARTED");

        // After you run your desired action:
        // clear search bar
        searchView.setQuery("", false);

        // collapse the search box back to the menu icon
        searchView.setIconified(true);

        // clear the focus of the SearchView and
        View current = getCurrentFocus();
        if (current != null)
            current.clearFocus();

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
