package de.tum.`in`.tumcampusapp.component.other.general.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.RoomWarnings

@Entity
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class Recent(@PrimaryKey
                  var name: String = "",
                  var type: Int = -1)