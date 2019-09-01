package de.tum.`in`.tumcampusapp.component.ui.openinghour.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
import com.google.gson.annotations.SerializedName

/**
 * New Location
 *
 * @param id Location ID, e.g. 100
 * @param category Location category, e.g. library, cafeteria, info
 * @param name Location name, e.g. Studentenwerksbibliothek
 * @param address Address, e.g. Arcisstr. 21
 * @param room Room, e.g. MI 00.01.123
 * @param transport Transportation station name, e.g. U2 Königsplatz
 * @param hours Opening hours, e.g. Mo–Fr 8–24
 * @param remark Additional information, e.g. Tel: 089-11111
 * @param url Location URL, e.g. http://stud.ub.uni-muenchen.de/
 */
@Entity
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class Location(
    @PrimaryKey
    var id: Int = -1,
    var category: String = "",
    var name: String = "",
    var address: String = "",
    var room: String = "",
    @SerializedName("transport_station")
    var transport: String = "",
    @SerializedName("opening_hours")
    var hours: String = "",
    @SerializedName("infos")
    var remark: String = "",
    var url: String = "",
    @SerializedName("reference_id")
    var reference: Int = -1
)