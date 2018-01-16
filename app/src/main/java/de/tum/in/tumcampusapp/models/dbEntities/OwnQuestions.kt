package de.tum.`in`.tumcampusapp.models.dbEntities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "ownQuestions")
data class OwnQuestions(@PrimaryKey
                         var id: String = "",
                         var lastSync: String = "")

//CREATE TABLE IF NOT EXISTS ownQuestions (question INTEGER PRIMARY KEY, text VARCHAR, targetFac VARCHAR, created VARCHAR, end VARCHAR, yes INTEGER, no INTEGER, deleted BOOLEAN, synced BOOLEAN)