package de.tum.in.tumcampusapp.component.ui.transportation.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;

import com.google.common.base.Optional;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.general.RecentsDao;
import de.tum.in.tumcampusapp.component.other.general.model.Recent;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForSearchingInBackground;
import de.tum.in.tumcampusapp.component.other.generic.adapter.NoResultsAdapter;
import de.tum.in.tumcampusapp.component.ui.transportation.MVVStationSuggestionProvider;
import de.tum.in.tumcampusapp.component.ui.transportation.TransportController;
import de.tum.in.tumcampusapp.component.ui.transportation.model.efa.StationResult;
import de.tum.in.tumcampusapp.component.ui.transportation.model.efa.WidgetDepartures;

public class MVVWidgetConfigureActivity extends ActivityForSearchingInBackground<List<StationResult>> implements AdapterView.OnItemClickListener {

    private int appWidgetId;
    private ListView listViewResults;
    private ArrayAdapter<StationResult> adapterStations;
    private RecentsDao recentsDao;

    private WidgetDepartures widgetDepartures;

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

        TransportController tm = new TransportController(this);
        this.widgetDepartures = tm.getWidget(appWidgetId);

        Switch autoReloadSwitch = findViewById(R.id.mvv_widget_auto_reload);
        autoReloadSwitch.setChecked(this.widgetDepartures.getAutoReload());
        autoReloadSwitch.setOnCheckedChangeListener((compoundButton, checked) -> widgetDepartures.setAutoReload(checked));
        // TODO add handling for use location

        listViewResults = findViewById(R.id.activity_transport_listview_result);
        listViewResults.setOnItemClickListener(this);

        // Initialize stations adapter
        List<Recent> recentStations = recentsDao.getAll(RecentsDao.STATIONS);
        adapterStations = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, TransportController.getRecentStations(recentStations));

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
        StationResult stationResult = (StationResult) av.getAdapter()
                                                        .getItem(position);
        widgetDepartures.setStation(stationResult.getStation());
        widgetDepartures.setStationId(stationResult.getId());
        saveAndReturn();
    }

    /**
     * Shows all recently used stations
     *
     * @return List of StationResult
     */
    @Override
    public Optional<List<StationResult>> onSearchInBackground() {
        return Optional.of(TransportController.getRecentStations(recentsDao.getAll(RecentsDao.STATIONS)));
    }

    /**
     * Searches the Webservice for stations
     *
     * @param query the text entered by the user
     * @return List of StationResult
     */
    @Override
    public Optional<List<StationResult>> onSearchInBackground(String query) {
        // Get Information
        List<StationResult> stations = TransportController.getStationsFromExternal(this, query);

        return Optional.of(stations);
    }

    /**
     * Shows the stations
     *
     * @param possibleStationList list of possible StationResult
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Override
    protected void onSearchFinished(Optional<List<StationResult>> possibleStationList) {
        if (!possibleStationList.isPresent()) {
            return;
        }
        List<StationResult> stationResultList = possibleStationList.get();

        showLoadingEnded();

        // mQuery is not null if it was a real search
        if (stationResultList.isEmpty()) {
            // So show no results found
            listViewResults.setAdapter(new NoResultsAdapter(this));
            listViewResults.requestFocus();
            return;
        }

        adapterStations.clear();
        adapterStations.addAll(stationResultList);

        adapterStations.notifyDataSetChanged();
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
     */
    private void saveAndReturn() {
        // save the settingsPrefix
        TransportController transportManager = new TransportController(this);
        transportManager.addWidget(appWidgetId, this.widgetDepartures);

        // update alarms
        MVVWidget.setAlarm(this);

        // update widget
        Intent reloadIntent = new Intent(this, MVVWidget.class);
        reloadIntent.setAction(MVVWidget.MVV_WIDGET_FORCE_RELOAD);
        reloadIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        sendBroadcast(reloadIntent);

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
        if (!widgetDepartures.getStation()
                             .isEmpty() && !widgetDepartures.getStationId()
                                                            .isEmpty()) {
            saveAndReturn();
        } else {
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_CANCELED, resultValue);
            finish();
        }
    }
}
