package de.tum.`in`.tumcampusapp.models.dbEntities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "openQuestions")
data class OpenQuestions(@PrimaryKey
                         var question: Int = -1,
                         var text: String = "",
                         var created: String = "",
                         var end: String = "",
                         var answerid: Int = -1,
                         var answered: Boolean = false,
                         var synced: Boolean = false)