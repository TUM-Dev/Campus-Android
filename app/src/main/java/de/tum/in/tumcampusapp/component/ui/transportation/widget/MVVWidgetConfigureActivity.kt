package de.tum.`in`.tumcampusapp.component.ui.transportation.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Switch
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.general.RecentsDao
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForSearching
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.NoResultsAdapter
import de.tum.`in`.tumcampusapp.component.ui.transportation.MVVStationSuggestionProvider
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportController
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.StationResult
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.WidgetDepartures
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MVVWidgetConfigureActivity :
        ActivityForSearching(R.layout.activity_mvv_widget_configure, MVVStationSuggestionProvider.AUTHORITY, 3),
        AdapterView.OnItemClickListener {

    private var appWidgetId: Int = 0
    private lateinit var listViewResults: ListView
    private lateinit var adapterStations: ArrayAdapter<StationResult>
    private lateinit var recentsDao: RecentsDao

    private lateinit var widgetDepartures: WidgetDepartures

    private val disposable = CompositeDisposable()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recentsDao = TcaDb.getInstance(this).recentsDao()

        // Setup cancel button
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_action_cancel)

        // Get appWidgetId from intent
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        val tm = TransportController(this)
        this.widgetDepartures = tm.getWidget(appWidgetId)

        val autoReloadSwitch = findViewById<Switch>(R.id.mvv_widget_auto_reload)
        autoReloadSwitch.isChecked = this.widgetDepartures.autoReload
        autoReloadSwitch.setOnCheckedChangeListener { _, checked ->
            widgetDepartures.autoReload = checked
        }
        // TODO add handling for use location

        listViewResults = findViewById(R.id.activity_transport_listview_result)
        listViewResults.onItemClickListener = this

        // Initialize stations adapter
        val recentStations = recentsDao.getAll(RecentsDao.STATIONS) ?: emptyList()
        adapterStations = ArrayAdapter(this, android.R.layout.simple_list_item_1,
                TransportController.getRecentStations(recentStations))

        if (adapterStations.count == 0) {
            openSearch()
            return
        }
        listViewResults.adapter = adapterStations
    }

    /**
     * Click on station in list
     */
    override fun onItemClick(av: AdapterView<*>, v: View, position: Int, id: Long) {
        val (station, stationId) = av.adapter.getItem(position) as StationResult
        widgetDepartures.station = station
        widgetDepartures.stationId = stationId
        saveAndReturn()
    }

    override fun onStartSearch() {
        val recents = recentsDao.getAll(RecentsDao.STATIONS)
        if (recents == null) {
            listViewResults.adapter = NoResultsAdapter(this)
            return
        }

        val stations = TransportController.getRecentStations(recents)
        displayStations(stations)
    }

    override fun onStartSearch(query: String) {
        disposable.add(TransportController.getStationsFromExternal(this, query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::displayStations) {
                    // Something went wrong
                    Utils.showToast(this, R.string.something_wrong)
                    Utils.log(it)
                    onStartSearch()
                })
    }

    private fun displayStations(stations: List<StationResult>) {
        showLoadingEnded()

        if (stations.isEmpty()) {
            listViewResults.adapter = NoResultsAdapter(this)
            return
        }

        adapterStations.clear()
        adapterStations.addAll(stations)

        adapterStations.notifyDataSetChanged()
        listViewResults.adapter = adapterStations
    }

    /**
     * Setup cancel and back action
     *
     * @param item the menu item which has been pressed (or activated)
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                cancelAndReturn()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Saves the selection to the database, triggers a widget update and closes this activity
     */
    private fun saveAndReturn() {
        // save the settingsPrefix
        val transportManager = TransportController(this)
        transportManager.addWidget(appWidgetId, widgetDepartures)

        // update alarms
        MVVWidget.setAlarm(this)

        // update widget
        val reloadIntent = Intent(this, MVVWidget::class.java)
        reloadIntent.action = MVVWidget.MVV_WIDGET_FORCE_RELOAD
        reloadIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        sendBroadcast(reloadIntent)

        // return to widget
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    /**
     * Cancel the widget creation and close this activity
     */
    private fun cancelAndReturn() {
        val resultValue = Intent()
        if (!(widgetDepartures.station.isEmpty() || widgetDepartures.stationId.isEmpty())) {
            saveAndReturn()
        } else {
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_CANCELED, resultValue)
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }
}
