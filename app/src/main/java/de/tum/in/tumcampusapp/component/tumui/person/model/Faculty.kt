package de.tum.`in`.tumcampusapp.component.tumui.person.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.RoomWarnings

/**
 * Presents the faculty model that is used in fetching the facultyData from server
 * @param faculty the id of the faculty
 */
@Entity(tableName = "faculties")
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class Faculty(@PrimaryKey
                   var faculty: String = "",
                   var name: String = "")