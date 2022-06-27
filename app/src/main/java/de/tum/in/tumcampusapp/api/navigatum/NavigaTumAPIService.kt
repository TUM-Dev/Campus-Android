package de.tum.`in`.tumcampusapp.api.navigatum

import de.tum.`in`.tumcampusapp.api.navigatum.domain.NavigationEntity
import de.tum.`in`.tumcampusapp.api.navigatum.model.details.NavigationDetailsDto
import de.tum.`in`.tumcampusapp.api.navigatum.model.search.NavigaTumSearchResponseDto
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NavigaTumAPIService {

    @GET(API_SEARCH)
    fun searchNavigation(
        @Query(QUERY_PARAM) query: String
    ): Single<NavigaTumSearchResponseDto>

    @GET(API_SEARCH)
    fun fetchRooms(
        @Query(QUERY_PARAM) query: String
    ): Call<List<NavigationEntity>>

    @GET(API_GET)
    fun getNavigationDetails(
        @Path(ID_PARAM) id: String
    ): Single<NavigationDetailsDto>

    companion object {
        private const val API_SEARCH = "api/search"
        private const val QUERY_PARAM = "q"
        private const val ID_PARAM = "id"
        private const val API_GET = "api/get/{$ID_PARAM}"
    }
}
