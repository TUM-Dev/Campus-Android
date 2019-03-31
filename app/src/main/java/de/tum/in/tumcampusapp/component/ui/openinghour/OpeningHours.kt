package de.tum.`in`.tumcampusapp.component.ui.openinghour

object OpeningHours {
    data class Result(val list: List<Opening>)
    data class Opening(
            val id: Int,
            val category: String,
            val name: String,
            val address: String,
            val room: String,
            val transportStation: String,
            val openingHours: String,
            val infos: String,
            val url: String
    )
}