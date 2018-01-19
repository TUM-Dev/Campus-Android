package de.tum.`in`.tumcampusapp.models.efa

import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.models.dbEntities.Recent

data class StationResult(var station: String = "",
                         var id: String = "",
                         var quality: Int = 0) {

    override fun toString(): String = station

    companion object {
        fun fromRecent(r: Recent): StationResult? {
            return Gson().fromJson(r.name, StationResult::class.java)
        }
    }
}