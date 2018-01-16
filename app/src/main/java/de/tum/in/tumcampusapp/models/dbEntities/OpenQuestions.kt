package de.tum.`in`.tumcampusapp.models.dbEntities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "openQuestions")
data class OpenQuestions(@PrimaryKey
                var id: String = "",
                var lastSync: String = "")

//CREATE TABLE IF NOT EXISTS openQuestions (question INTEGER PRIMARY KEY, text VARCHAR, created VARCHAR, end VARCHAR, answerid INTEGER, answered BOOLEAN, synced BOOLEAN)