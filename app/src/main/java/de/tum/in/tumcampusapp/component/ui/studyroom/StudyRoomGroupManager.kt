package de.tum.`in`.tumcampusapp.component.ui.studyroom

import android.content.Context
import de.tum.`in`.tumcampusapp.component.ui.studyroom.model.StudyRoom
import de.tum.`in`.tumcampusapp.component.ui.studyroom.model.StudyRoomGroup
import de.tum.`in`.tumcampusapp.database.TcaDb
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//import org.jetbrains.anko.doAsync

/**
 * Handles content for the study room feature, fetches external data.
 */
class StudyRoomGroupManager(context: Context) {

    private val roomsDao: StudyRoomDao
    private val groupsDao: StudyRoomGroupDao

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    init {
        val db = TcaDb.getInstance(context)
        roomsDao = db.studyRoomDao()
        groupsDao = db.studyRoomGroupDao()
    }

    suspend fun updateDatabase(groups: List<StudyRoomGroup>) {
        // moves to IOThread to not block MainThread
        withContext(ioDispatcher){
            groupsDao.removeCache()
            roomsDao.removeCache()

            groupsDao.insert(*groups.toTypedArray())

            groups.forEach { group ->
                group.rooms.forEach { room ->
                    // only insert rooms that have data
                    if (room.code != "" &&
                            room.name != "" &&
                            room.buildingName != "" &&
                            room.id != -1) {
                        roomsDao.insert(room)
                    }
                }
            }
        }
    }

    fun getAllStudyRoomsForGroup(groupId: Int): List<StudyRoom> {
        return roomsDao.getAll(groupId).sorted()
    }
}
