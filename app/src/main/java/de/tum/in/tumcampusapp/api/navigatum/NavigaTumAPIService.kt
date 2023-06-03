package de.tum.`in`.tumcampusapp.api.navigatum

import de.tum.`in`.tumcampusapp.api.navigatum.model.details.NavigationDetailsDto
import de.tum.`in`.tumcampusapp.api.navigatum.model.search.NavigaTumSearchResponseDto
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NavigaTumAPIService {

    @GET(API_SEARCH)
    fun search(
        @Query(QUERY_PARAM) query: String
    ): Call<NavigaTumSearchResponseDto>

    @GET(API_GET)
    fun getNavigationDetails(
        @Path(ID_PARAM) id: String,
        @Query(LANG_PARAM) lang: String
    ): Call<NavigationDetailsDto>

    @GET(API_SEARCH)
    fun searchSingle(
        @Query(QUERY_PARAM) query: String
    ): Single<NavigaTumSearchResponseDto>

    @GET(API_GET)
    fun getNavigationDetailsSingle(
        @Path(ID_PARAM) id: String,
        @Query(LANG_PARAM) lang: String
    ): Single<NavigationDetailsDto>

    companion object {
        private const val API_SEARCH = "api/search"
        private const val QUERY_PARAM = "q"
        private const val ID_PARAM = "id"
        private const val API_GET = "api/get/{$ID_PARAM}"
        private const val LANG_PARAM = "lang"
    }
}
