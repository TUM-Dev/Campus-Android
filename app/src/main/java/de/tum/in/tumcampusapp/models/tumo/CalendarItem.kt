package de.tum.`in`.tumcampusapp.models.tumo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

@Entity(tableName="calendar")
data class CalendarItem(@PrimaryKey
                        var nr: String = "",
                        var status: String = "",
                        var url: String = "",
                        var title: String = "",
                        var description: String = "",
                        var dtstart: String = "",
                        var dtend: String = "",
                        var location: String = "",
                        @Ignore
                        var blacklisted: Boolean = false)