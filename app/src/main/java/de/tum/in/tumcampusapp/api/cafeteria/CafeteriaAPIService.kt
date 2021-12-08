package de.tum.`in`.tumcampusapp.api.cafeteria

import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface CafeteriaAPIService {

    @GET("all.json")
    fun getMenus(
        @Header("Cache-Control") cacheControl: String
    ): Call<CafeteriaResponse>
}