package de.tum.`in`.tumcampusapp.api.cafeteria

import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaLocation
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization.CafeteriaResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface CafeteriaAPIService {

    @GET("{cafeteriaSlug}/{year}/{calendarWeek}.json")
    fun getMenus(
        @Header("Cache-Control") cacheControl: String,
        @Path("cafeteriaSlug") cafeteriaSlug: String,
        @Path("year") year: Int,
        @Path("calendarWeek") calendarWeek: String
    ): Call<CafeteriaResponse>
}