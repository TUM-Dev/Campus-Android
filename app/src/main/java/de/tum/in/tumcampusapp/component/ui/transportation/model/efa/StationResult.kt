package de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa

import com.google.gson.Gson
import com.google.gson.JsonObject
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent

data class StationResult(var station: String = "",
                         var id: String = "",
                         var quality: Int = 0) {

    override fun toString(): String = station

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