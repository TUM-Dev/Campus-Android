package de.tum.in.tumcampusapp.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.common.base.Optional;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForSearchingInBackground;
import de.tum.in.tumcampusapp.adapters.NoResultsAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.MVVStationSuggestionProvider;
import de.tum.in.tumcampusapp.managers.RecentsManager;
import de.tum.in.tumcampusapp.managers.TransportManager;

public class MVVWidgetConfigureActivity extends ActivityForSearchingInBackground<Cursor> implements AdapterView.OnItemClickListener {

    private int appWidgetId;
    private ListView listViewResults;
    private SimpleCursorAdapter adapterStations;
    private RecentsManager recentsManager;

    public MVVWidgetConfigureActivity() {
        super(R.layout.activity_mvv_widget_configure, MVVStationSuggestionProvider.AUTHORITY, 3);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup cancel button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_cancel);

        // Get appWidgetId from intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // Det all stations from db
        recentsManager = new RecentsManager(this, RecentsManager.STATIONS);

        listViewResults = (ListView) findViewById(R.id.activity_transport_listview_result);
        listViewResults.setOnItemClickListener(this);

        // Initialize stations adapter
        Cursor stationCursor = recentsManager.getAllFromDb();
        adapterStations = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, stationCursor,
                stationCursor.getColumnNames(), new int[]{android.R.id.text1}, 0);

        if (adapterStations.getCount() == 0) {
            openSearch();
        } else {
            listViewResults.setAdapter(adapterStations);
            listViewResults.requestFocus();
        }
    }

    /**
     * Click on station in list
     */
    @Override
    public void onItemClick(final AdapterView<?> av, View v, int position, long id) {
        Cursor departureCursor = (Cursor) av.getAdapter().getItem(position);
        saveAndReturn(departureCursor.getString(departureCursor.getColumnIndex(Const.NAME_COLUMN)),
                departureCursor.getString(departureCursor.getColumnIndex(Const.ID_COLUMN)), false);
    }


    /**
     * Shows all recently used stations
     *
     * @return Cursor holding the recents information (name, _id)
     */
    @Override
    public Optional<Cursor> onSearchInBackground() {
        return Optional.of(recentsManager.getAllFromDb());
    }

    /**
     * Searches the Webservice for stations
     *
     * @param query the text entered by the user
     * @return Cursor holding the stations (name, _id)
     */
    @Override
    public Optional<Cursor> onSearchInBackground(String query) {
        // Get Information
        Optional<Cursor> stationCursor = TransportManager.getStationsFromExternal(this, query);
        if (!stationCursor.isPresent()) {
            showError(R.string.exception_unknown);
        }

        // Drop results if canceled
        if (asyncTask.isCancelled()) {
            return Optional.absent();
        }

        return stationCursor;
    }

    /**
     * Shows the stations
     *
     * @param possibleStationCursor Cursor with stations (name, _id)
     */
    @Override
    protected void onSearchFinished(Optional<Cursor> possibleStationCursor) {
        if (!possibleStationCursor.isPresent()) {
            return;
        }
        Cursor stationCursor = possibleStationCursor.get();

        showLoadingEnded();

        // mQuery is not null if it was a real search
        if (stationCursor.getCount() == 0) {
            // When stationCursor is a MatrixCursor the result comes from querying a station name
            if (stationCursor instanceof MatrixCursor) {
                // So show no results found
                listViewResults.setAdapter(new NoResultsAdapter(this));
                listViewResults.requestFocus();
            } else {
                // if the loading came from the user canceling search
                // and there are no recents to show close activity
                cancelAndReturn();
            }
            return;
        }

        adapterStations.changeCursor(stationCursor);
        listViewResults.setAdapter(adapterStations);
        listViewResults.requestFocus();
    }

    /**
     * Setup cancel and back action
     *
     * @param item the menu item which has been pressed (or activated)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                cancelAndReturn();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Saves the selection to the database, triggers a widget update and closes this activity
     *
     * @param station      the selected station
     * @param station_id   the id of the selected station
     * @param use_location [not implemented yet] whether the station should be selected automatically by the nearest station
     */
    private void saveAndReturn(String station, String station_id, boolean use_location) {
        // save the settings
        TransportManager transportManager = new TransportManager(this);
        transportManager.addWidget(appWidgetId, station, station_id, use_location);

        // update widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        MVVWidget.updateAppWidget(this, appWidgetManager, appWidgetId);

        // return to widget
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    /**
     * Cancel the widget creation and close this activity
     */
    private void cancelAndReturn() {
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_CANCELED, resultValue);
        finish();
    }
}
