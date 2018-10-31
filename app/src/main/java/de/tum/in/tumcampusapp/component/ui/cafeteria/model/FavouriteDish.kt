package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings

@Entity
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class FavoriteDish(@PrimaryKey(autoGenerate = true)
                        var id: Int = 0,
                        var cafeteriaId: Int = -1,
                        var dishName: String = "",
                        var date: String = "",
                        var tag: String = "") {
    companion object {
        @JvmStatic
        fun create(cafeteriaId: Int, dishName: String, date: String, tag: String) = FavoriteDish(
                cafeteriaId = cafeteriaId,
                dishName = dishName,
                date = date,
                tag = tag)
    }
}