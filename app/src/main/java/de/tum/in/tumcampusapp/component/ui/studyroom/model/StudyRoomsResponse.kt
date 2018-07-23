package de.tum.`in`.tumcampusapp.component.ui.studyroom.model

import com.google.gson.annotations.SerializedName

data class StudyRoomsResponse(
        @SerializedName("raeume") val rooms: List<StudyRoom>,
        @SerializedName("gruppen") val groups: List<StudyRoomGroup>
)