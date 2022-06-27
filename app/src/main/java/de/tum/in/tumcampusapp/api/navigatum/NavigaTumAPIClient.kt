package de.tum.`in`.tumcampusapp.api.navigatum

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.ApiHelper
import de.tum.`in`.tumcampusapp.api.navigatum.domain.NavigationEntity
import de.tum.`in`.tumcampusapp.api.navigatum.model.details.NavigationDetailsDto
import de.tum.`in`.tumcampusapp.api.navigatum.model.search.NavigaTumSearchResponseDto
import de.tum.`in`.tumcampusapp.api.tumonline.interceptors.CacheResponseInterceptor
import de.tum.`in`.tumcampusapp.utils.CacheManager
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class NavigaTumAPIClient(private val apiService: NavigaTumAPIService) {

    fun search(query: String): Single<NavigaTumSearchResponseDto> {
        return apiService.searchNavigation(query)
    }

    fun getNavigationDetails(id: String): Single<NavigationDetailsDto> {
        return apiService.getNavigationDetails(id)
    }

    fun fetchRooms(query: String): List<NavigationEntity>? {
        return apiService.fetchRooms(query)
            .execute()
            .body()
    }

    companion object {
        private const val BASE_URL = "https://nav.tum.sexy/"

        private var apiClient: NavigaTumAPIClient? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): NavigaTumAPIClient {
            if (apiClient == null) {
                apiClient = buildAPIClient(context)
            }
            return apiClient!!
        }

        private fun buildAPIClient(context: Context): NavigaTumAPIClient {
            val cacheManager = CacheManager(context)

            val client = ApiHelper.getOkHttpClient(context)
                .newBuilder()
                .cache(cacheManager.cache)
                .addNetworkInterceptor(CacheResponseInterceptor())
                .build()

            val apiService = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(NavigaTumAPIService::class.java)

            return NavigaTumAPIClient(apiService)
        }
    }
}
