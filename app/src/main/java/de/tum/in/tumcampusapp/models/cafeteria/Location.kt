package de.tum.`in`.tumcampusapp.models.cafeteria

/**
 * New Location
 *
 * @param id        Location ID, e.g. 100
 * @param category  Location category, e.g. library, cafeteria, info
 * @param name      Location name, e.g. Studentenwerksbibliothek
 * @param address   Address, e.g. Arcisstr. 21
 * @param room      Room, e.g. MI 00.01.123
 * @param transport Transportation station name, e.g. U2 Königsplatz
 * @param hours     Opening hours, e.g. Mo–Fr 8–24
 * @param remark    Additional information, e.g. Tel: 089-11111
 * @param url       Location URL, e.g. http://stud.ub.uni-muenchen.de/
 */
data class Location(val id: Int, val category: String, val name: String, val address: String, val room: String, val transport: String, val hours: String, val remark: String, val url: String)