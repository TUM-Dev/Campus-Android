package de.tum.`in`.tumcampusapp.component.ui.studyroom

import de.tum.`in`.tumcampusapp.component.ui.studyroom.model.StudyRoom
import de.tum.`in`.tumcampusapp.component.ui.studyroom.model.StudyRoomGroup
import de.tum.`in`.tumcampusapp.database.TcaDb
import org.jetbrains.anko.doAsync
import javax.inject.Inject

/**
 * Handles content for the study room feature, fetches external data.
 */
class StudyRoomGroupLocalRepository @Inject constructor(
        private val database: TcaDb
) {

    fun updateDatabase(groups: List<StudyRoomGroup>, callback: () -> Unit) {
        doAsync {
            database.studyRoomGroupDao().removeCache()
            database.studyRoomDao().removeCache()

            database.studyRoomGroupDao().insert(*groups.toTypedArray())

            groups.forEach { group ->
                database.studyRoomDao().insert(*group.rooms.toTypedArray())
            }

            callback()
        }
    }

    fun getAllStudyRoomsForGroup(groupId: Int): List<StudyRoom> {
        return database.studyRoomDao().getAll(groupId).sorted()
    }

}
