package de.tum.`in`.tumcampusapp.models.cafeteria

/**
 * new Cafeteria
 *
 * @param id        Cafeteria ID, e.g. 412
 * @param name      Name, e.g. MensaX
 * @param address   Address, e.g. Boltzmannstr. 3
 * @param latitude  Coordinates of the cafeteria
 * @param longitude Coordinates of the cafeteria
 */
data class Cafeteria(val id: Int, val name: String, val address: String, val latitude: Double, val longitude: Double) : Comparable<Cafeteria> {

    // Used for ordering cafeterias
    var distance: Float = 0.toFloat()

    override fun toString(): String {
        return name
    }

    override fun compareTo(other: Cafeteria): Int {
        return java.lang.Float.compare(distance, other.distance)
    }
}