package de.tum.`in`.tumcampusapp.component.ui.barrierfree

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumCabe
import de.tum.`in`.tumcampusapp.component.other.locations.LocationManager
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.NavigationDetailsActivity
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.NavigationDetailsFragment
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderListAdapter
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model.RoomFinderRoom
import de.tum.`in`.tumcampusapp.databinding.ActivityBarrierFreeFacilitiesBinding
import retrofit2.Call

class BarrierFreeFacilitiesActivity :
    ActivityForAccessingTumCabe<List<RoomFinderRoom>>(
        R.layout.activity_barrier_free_facilities
    ),
    AdapterView.OnItemSelectedListener {

    private val locationManager: LocationManager by lazy {
        LocationManager(this)
    }

    private lateinit var binding: ActivityBarrierFreeFacilitiesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBarrierFreeFacilitiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.spinnerToolbar.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        when (position) {
            0 -> fetchApiCallForCurrentLocation()
            1 -> executeApiCall(apiClient.listOfElevators)
            else -> executeApiCall(apiClient.listOfElevators)
        }
    }

    private fun fetchApiCallForCurrentLocation() {
        locationManager.fetchBuildingIDFromCurrentLocation {
            val apiCall = apiClient.getListOfNearbyFacilities(it)
            executeApiCall(apiCall)
        }
    }

    private fun executeApiCall(apiCall: Call<List<RoomFinderRoom>>?) {
        apiCall?.let { fetch(it) } ?: showError(R.string.error_something_wrong)
    }

    override fun onDownloadSuccessful(response: List<RoomFinderRoom>) {
        binding.barrierFreeFacilitiesListView.adapter = RoomFinderListAdapter(this, response)
        binding.barrierFreeFacilitiesListView.setOnItemClickListener { _, _, index, _ ->
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
