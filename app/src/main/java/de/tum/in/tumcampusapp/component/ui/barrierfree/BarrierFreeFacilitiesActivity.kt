package de.tum.`in`.tumcampusapp.component.ui.barrierfree

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.lifecycle.lifecycleScope
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumCabe
import de.tum.`in`.tumcampusapp.component.other.locations.LocationManager
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.NavigationDetailsActivity
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.NavigationDetailsFragment
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderListAdapter
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model.RoomFinderRoom
import kotlinx.coroutines.launch
import retrofit2.Call
import se.emilsjolander.stickylistheaders.StickyListHeadersListView

class BarrierFreeFacilitiesActivity :
    ActivityForAccessingTumCabe<List<RoomFinderRoom>>(
        R.layout.activity_barrier_free_facilities
    ),
    AdapterView.OnItemSelectedListener {

    private val locationManager: LocationManager by lazy {
        LocationManager(this)
    }

    private lateinit var listView: StickyListHeadersListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listView = findViewById(R.id.barrierFreeFacilitiesListView)
        val spinnerToolbar = findViewById<Spinner>(R.id.spinnerToolbar)
        spinnerToolbar.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        when (position) {
            0 -> fetchApiCallForCurrentLocation()
            1 -> executeApiCall(apiClient.listOfToilets)
            else -> executeApiCall(apiClient.listOfElevators)
        }
    }

    private fun fetchApiCallForCurrentLocation() {
        this.lifecycleScope.launch {
            locationManager.fetchBuildingIDFromCurrentLocation {
                val apiCall = apiClient.getListOfNearbyFacilities(it)
                executeApiCall(apiCall)
            }
        }
    }

    private fun executeApiCall(apiCall: Call<List<RoomFinderRoom>>?) {
        this@BarrierFreeFacilitiesActivity.runOnUiThread {
            apiCall?.let { fetch(it) } ?: showError(R.string.error_something_wrong)
        }
    }

    override fun onDownloadSuccessful(response: List<RoomFinderRoom>) {
        listView.adapter = RoomFinderListAdapter(this, response)
        listView.setOnItemClickListener { _, _, index, _ ->
            val facility = response[index]
            openNavigationDetails(facility)
        }
    }

    private fun openNavigationDetails(facility: RoomFinderRoom) {
        val intent = Intent(this, NavigationDetailsActivity::class.java)
        intent.putExtra(NavigationDetailsFragment.NAVIGATION_ENTITY_ID, facility.room_code)
        startActivity(intent)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Nothing selected
    }
}
