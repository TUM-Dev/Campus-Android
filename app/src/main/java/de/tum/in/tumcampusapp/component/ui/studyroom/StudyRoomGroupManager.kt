package de.tum.`in`.tumcampusapp.component.ui.studyroom

import android.content.Context
import de.tum.`in`.tumcampusapp.component.ui.studyroom.model.StudyRoom
import de.tum.`in`.tumcampusapp.component.ui.studyroom.model.StudyRoomGroup
import de.tum.`in`.tumcampusapp.database.TcaDb
import org.jetbrains.anko.doAsync

/**
 * Handles content for the study room feature, fetches external data.
 */
class StudyRoomGroupManager(context: Context) {

    private val roomsDao: StudyRoomDao
    private val groupsDao: StudyRoomGroupDao

    init {
        val db = TcaDb.getInstance(context)
        roomsDao = db.studyRoomDao()
        groupsDao = db.studyRoomGroupDao()
    }

    fun updateDatabase(groups: List<StudyRoomGroup>, callback: () -> Unit) {
        doAsync {
            groupsDao.removeCache()
            roomsDao.removeCache()

            groupsDao.insert(*groups.toTypedArray())

            groups.forEach { group ->
                roomsDao.insert(*group.rooms.toTypedArray())
            }

            callback()
        }
    }

    fun getAllStudyRoomsForGroup(groupId: Int): List<StudyRoom> {
        return roomsDao.getAll(groupId).sorted()
    }

}
