package de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportationDetailsActivity

data class StationResult(var station: String = "",
                         var id: String = "",
                         var quality: Int = 0) {

    override fun toString(): String = station

    fun getIntent(context: Context): Intent {
        return Intent(context, TransportationDetailsActivity::class.java).apply {
            putExtra(TransportationDetailsActivity.EXTRA_STATION, id)
            putExtra(TransportationDetailsActivity.EXTRA_STATION_ID, station)
        }
    }

    companion object {
        fun fromRecent(r: Recent): StationResult? {
            return Gson().fromJson(r.name, StationResult::class.java)
        }
    }
}