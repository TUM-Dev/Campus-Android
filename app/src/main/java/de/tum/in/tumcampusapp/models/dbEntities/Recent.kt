package de.tum.`in`.tumcampusapp.models.dbEntities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class Recent(@PrimaryKey
                  var name: String = "",
                  var type: Int = -1)