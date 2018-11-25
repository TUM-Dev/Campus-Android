package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings

/**
 * new Cafeteria
 *
 * @param id        Cafeteria ID, e.g. 412
 * @param name      Name, e.g. MensaX
 * @param address   Address, e.g. Boltzmannstr. 3
 * @param latitude  Coordinates of the cafeteria
 * @param longitude Coordinates of the cafeteria
 */
@Entity
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class Cafeteria(
        @field:PrimaryKey
        var id: Int = -1,
        var name: String = "",
        var address: String = "",
        var latitude: Double = -1.0,
        var longitude: Double = -1.0
) : Comparable<Cafeteria> {

    // Used for ordering cafeterias
    var distance: Float = 0f

    override fun toString(): String = name

    override fun compareTo(other: Cafeteria): Int = java.lang.Float.compare(distance, other.distance)
}