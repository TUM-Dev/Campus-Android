package de.tum.`in`.tumcampusapp.models.dbEntities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "ownQuestions")
data class OwnQuestions(@PrimaryKey
                        var question: Int = -1,
                        var text: String = "",
                        var targetFac: String = "",
                        var created: String = "",
                        var end: String = "",
                        var yes: Int = -1,
                        var no: Int = -1,
                        var deleted: Boolean = false,
                        var synced: Boolean = false)