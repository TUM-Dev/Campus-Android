package de.tum.`in`.tumcampusapp.component.ui.chat.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.RoomWarnings
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.Lecture

@Entity(tableName = "chat_room", primaryKeys = ["name", "_id"])
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class ChatRoomDbRow(var room: Int = 0,
                         var name: String = "",
                         var semester: String = "",
                         @ColumnInfo(name = "semester_id")
                         var semesterId: String = "",
                         var joined: Int = 0,
                         @ColumnInfo(name = "_id")
                         var lvId: Int = 0,
                         var contributor: String = "",
                         var members: Int = -1,
                         @ColumnInfo(name = "last_read")
                         var lastRead: Int = -1) {


    companion object {
        fun fromLecture(lecture: Lecture): ChatRoomDbRow {
            return ChatRoomDbRow(-1, lecture.title, lecture.semesterName,
                    lecture.semesterId, -1, lecture.lectureId.toInt(),
                    lecture.lecturers ?: "", 0, -1)
        }
    }
}