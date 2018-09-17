package de.tum.`in`.tumcampusapp.component.ui.transportation

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.general.RecentsDao
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ProgressActivity
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.Departure
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.StationResult
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Activity to show transport departures for a specified station
 *
 *
 * NEEDS: EXTRA_STATION set in incoming bundle (station name)
 */
class TransportationDetailsActivity : ProgressActivity(R.layout.activity_transportation_detail) {

    private lateinit var mViewResults: LinearLayout
    private lateinit var recentsDao: RecentsDao
    private lateinit var transportManager: TransportController
    private lateinit var gson: Gson

    private val disposable = CompositeDisposable()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get all stations from db
        recentsDao = TcaDb.getInstance(this).recentsDao()
        transportManager = TransportController(this)
        gson = Gson()
        mViewResults = this.findViewById(R.id.activity_transport_result)

        val intent = intent
        if (intent == null) {
            finish()
            return
        }
        val location = intent.getStringExtra(EXTRA_STATION)
        title = location
        val locationID = intent.getStringExtra(EXTRA_STATION_ID)

        showLoadingStart()
        loadDetails(location, locationID)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_transport, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_transport_usage) {
            val dialog = AlertDialog.Builder(this)
                    .setTitle(R.string.transport_action_usage)
                    .setMessage(R.string.transport_help_text)
                    .setPositiveButton(android.R.string.ok, null)
                    .create()

            dialog.window.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
            dialog.show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadDetails(location: String, locationID: String) {
        // Quality is always 100% hit
        val stationResult = StationResult(location, locationID, Integer.MAX_VALUE)
        val jsonStationResult = gson.toJson(stationResult)

        // save clicked station into db
        recentsDao.insert(Recent(jsonStationResult, RecentsDao.STATIONS))

        disposable.add(TransportController.getDeparturesFromExternal(this, locationID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::displayResults) {
                    // something went wrong
                    Utils.log(it)
                    showError(R.string.no_departures_found)
                })
    }

    /**
     * Adds a new [DepartureView] for each departure entry
     *
     * @param results List of departures
     */
    private fun displayResults(results: List<Departure>?) {
        showLoadingEnded()
        if (results == null || results.isEmpty()) {
            showError(R.string.no_departures_found)
            return
        }
        mViewResults.removeAllViews()
        for ((_, direction, lineSymbol, _, departureTime) in results) {
            val view = DepartureView(this, true)

            view.setOnClickListener { v ->
                val departureView = v as DepartureView
                val symbol = departureView.symbol
                val highlight = if (transportManager.isFavorite(symbol)) {
                    transportManager.deleteFavorite(symbol)
                    false
                } else {
                    transportManager.addFavorite(symbol)
                    true
                }

                // Update the other views with the same symbol
                for (i in 0 until mViewResults.childCount) {
                    val child = mViewResults.getChildAt(i) as DepartureView
                    if (child.symbol == symbol) {
                        child.setSymbol(symbol, highlight)
                    }
                }
            }

            if (transportManager.isFavorite(lineSymbol)) {
                view.setSymbol(lineSymbol, true)
            } else {
                view.setSymbol(lineSymbol, false)
            }

            view.setLine(direction)
            view.setTime(departureTime)
            mViewResults.addView(view)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (i in 0 until mViewResults.childCount) {
            val view = mViewResults.getChildAt(i) as? DepartureView ?: continue
            view.removeAllCallbacksAndMessages()
        }
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    companion object {
        const val EXTRA_STATION = "station"
        const val EXTRA_STATION_ID = "stationID"
    }
}