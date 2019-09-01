package de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.JsonObject
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportationDetailsActivity

data class StationResult(
    var station: String = "",
    var id: String = "",
    var quality: Int = 0
) {

    override fun toString(): String = station

    fun getIntent(context: Context): Intent {
        return Intent(context, TransportationDetailsActivity::class.java).apply {
            putExtra(TransportationDetailsActivity.EXTRA_STATION_ID, id)
            putExtra(TransportationDetailsActivity.EXTRA_STATION, station)
        }
    }

    companion object {
        fun fromRecent(r: Recent): StationResult? {
            return Gson().fromJson(r.name, StationResult::class.java)
        }

        fun fromJson(json: JsonObject): StationResult {
            return StationResult(
                    json.get("name").asString,
                    json.getAsJsonObject("ref").get("id").asString,
                    json.get("quality")?.asInt ?: 0
            )
        }
    }
}