package de.tum.`in`.tumcampusapp.component.ui.transportation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.general.RecentsDao
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ProgressActivity
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.Departure
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.StationResult
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Activity to show transport departures for a specified station
 *
 *
 * NEEDS: EXTRA_STATION set in incoming bundle (station name)
 */
class TransportationDetailsActivity : ProgressActivity<Unit>(R.layout.activity_transportation_detail) {

    private val resultsView: LinearLayout by lazy {
        findViewById<LinearLayout>(R.id.activity_transport_result)
    }

    private val gson: Gson by lazy { Gson() }

    @Inject
    lateinit var database: TcaDb

    @Inject
    lateinit var transportController: TransportController

    private val disposable = CompositeDisposable()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)

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

            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
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
        database.recentsDao().insert(Recent(jsonStationResult, RecentsDao.STATIONS))

        disposable += transportController.fetchDeparturesAtStation(locationID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::displayResults) {
                    // Something went wrong
                    Utils.log(it)
                    showError(R.string.no_departures_found)
                }
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
        resultsView.removeAllViews()
        for ((_, direction, lineSymbol, _, departureTime) in results) {
            val view = DepartureView(this, true)

            view.setOnClickListener { v ->
                val departureView = v as DepartureView
                val symbol = departureView.symbol
                val highlight = if (transportController.isFavorite(symbol)) {
                    transportController.deleteFavorite(symbol)
                    false
                } else {
                    transportController.addFavorite(symbol)
                    true
                }

                // Update the other views with the same symbol
                for (i in 0 until resultsView.childCount) {
                    val child = resultsView.getChildAt(i) as DepartureView
                    if (child.symbol == symbol) {
                        child.setSymbol(symbol, highlight)
                    }
                }
            }

            if (transportController.isFavorite(lineSymbol)) {
                view.setSymbol(lineSymbol, true)
            } else {
                view.setSymbol(lineSymbol, false)
            }

            view.setLine(direction)
            view.setTime(departureTime)
            resultsView.addView(view)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (i in 0 until resultsView.childCount) {
            val view = resultsView.getChildAt(i) as? DepartureView ?: continue
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