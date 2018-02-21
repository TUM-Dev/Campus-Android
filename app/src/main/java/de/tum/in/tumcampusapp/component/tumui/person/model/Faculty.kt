package de.tum.`in`.tumcampusapp.component.tumui.person.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Presents the faculty model that is used in fetching the facultyData from server
 * @param faculty the id of the faculty
 */
@Entity(tableName = "faculties")
data class Faculty(@PrimaryKey
                   var faculty: String = "",
                   var name: String = "")