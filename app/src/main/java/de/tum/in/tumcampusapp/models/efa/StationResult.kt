package de.tum.`in`.tumcampusapp.models.efa

data class StationResult(var station: String = "",
                         var id: String = "",
                         var quality: Int = 0) {

    override fun toString(): String = station
}