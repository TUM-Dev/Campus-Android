package de.tum.`in`.tumcampusapp.models.cafeteria

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
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
@Entity
data class CafeteriaMenu(@PrimaryKey(autoGenerate = true)
                         var id: Int = 0,
                         var cafeteriaId: Int = -1,
                         var date: Date? = null,
                         var typeShort: String = "",
                         var typeLong: String = "",
                         var typeNr: Int = -1,
                         var name: String = "")