package de.tum.`in`.tumcampusapp.component.ui.studyroom

import android.content.Context
import de.tum.`in`.tumcampusapp.component.ui.studyroom.model.StudyRoom
import de.tum.`in`.tumcampusapp.component.ui.studyroom.model.StudyRoomsResponse
import de.tum.`in`.tumcampusapp.database.TcaDb

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

    fun updateDatabase(response: StudyRoomsResponse) {
        roomsDao.removeCache()
        groupsDao.removeCache()

        val groups = response.groups.toTypedArray()
        groupsDao.insert(*groups)

        val roomsById = response.rooms.associateBy { it.id }

        groups.forEach { group ->
            group.roomIds.forEach { roomId ->
                val room = roomsById[roomId]
                room?.let { it.studyRoomGroup = group.id }
            }
        }

        val rooms = roomsById.values.toTypedArray()
        roomsDao.insert(*rooms)

        // TODO: Needed?
        // SyncManager(context).replaceIntoDb(this)
    }

    fun getAllStudyRoomsForGroup(groupId: Int): List<StudyRoom> = roomsDao.getAll(groupId)

    companion object {
        @JvmField val STUDYROOM_HOST = "https://www.devapp.it.tum.de"
    }

}
