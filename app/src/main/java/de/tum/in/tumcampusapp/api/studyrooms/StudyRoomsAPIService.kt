package de.tum.`in`.tumcampusapp.api.studyrooms

import de.tum.`in`.tumcampusapp.component.ui.studyroom.model.StudyRoomsResponse
import retrofit2.Call
import retrofit2.http.GET

interface StudyRoomsAPIService {

    @GET("ris_api.php?format=json")
    fun getAll(): Call<StudyRoomsResponse>

}