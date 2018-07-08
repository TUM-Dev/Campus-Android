package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.RoomWarnings
import org.joda.time.DateTime

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
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class CafeteriaMenu(@PrimaryKey(autoGenerate = true)
                         var id: Int = 0,
                         var cafeteriaId: Int = -1,
                         var date: DateTime? = null,
                         var typeShort: String = "",
                         var typeLong: String = "",
                         var typeNr: Int = -1,
                         var name: String = "")