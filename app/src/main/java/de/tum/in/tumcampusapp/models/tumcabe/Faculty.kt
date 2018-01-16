package de.tum.`in`.tumcampusapp.models.tumcabe

import android.arch.persistence.room.Entity

/**
 * Presents the faculty model that is used in fetching the facultyData from server
 * @param faculty the id of the faculty
 */
@Entity
data class Faculty(var faculty: String = "",
                   var name: String = "")