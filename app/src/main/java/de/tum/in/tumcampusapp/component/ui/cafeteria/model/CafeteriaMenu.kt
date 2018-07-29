package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.RoomWarnings
import com.google.gson.annotations.SerializedName
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
data class CafeteriaMenu(
        @PrimaryKey(autoGenerate = true)
        @SerializedName("id")
        var id: Int = 0,
        @SerializedName("mensa_id")
        var cafeteriaId: Int = -1,
        @SerializedName("date")
        var date: DateTime? = null,
        @SerializedName("type_short")
        var typeShort: String = "",
        @SerializedName("type_long")
        var typeLong: String = "",
        // If a menu does not have a type number, it is a
        // side dish and is assigned type number 10
        @SerializedName("type_nr")
        var typeNr: Int = 10,
        @SerializedName("name")
        var name: String = ""
)