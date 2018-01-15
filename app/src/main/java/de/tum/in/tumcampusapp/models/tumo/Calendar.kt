package de.tum.`in`.tumcampusapp.models.tumo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import de.tum.`in`.tumcampusapp.models.dbEntities.RoomLocations

@Entity(tableName="calendar",
        foreignKeys = arrayOf(ForeignKey(entity = RoomLocations::class,
                                         parentColumns = arrayOf("title"),
                                         childColumns = arrayOf("location"),
                                         onDelete = ForeignKey.CASCADE)))
data class Calendar(@PrimaryKey
                    var nr: String = "",
                    var status: String = "",
                    var url: String = "",
                    var title: String = "",
                    var description: String = "",
                    var dtstart: String = "",
                    var dtend: String = "",
                    var location: String = "")