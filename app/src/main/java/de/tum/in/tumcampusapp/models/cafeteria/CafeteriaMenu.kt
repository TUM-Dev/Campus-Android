package de.tum.`in`.tumcampusapp.models.cafeteria

import java.util.*

/**
 * CafeteriaMenu
 *
 * @param id          CafeteriaMenu Id (empty for addendum)
 * @param cafeteriaId Cafeteria ID
 * @param date        Menu date
 * @param typeShort   Short type, e.g. tg
 * @param typeLong    Long type, e.g. Tagesgericht 1
 * @param typeNr      Type ID
 * @param name        Menu name
 */
data class CafeteriaMenu(val id: Int, val cafeteriaId: Int, val date: Date?, val typeShort: String?, val typeLong: String?, val typeNr: Int, val name: String?)