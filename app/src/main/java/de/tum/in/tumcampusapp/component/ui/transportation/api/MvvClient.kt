package de.tum.`in`.tumcampusapp.component.ui.transportation.api

import android.content.Context
import com.google.gson.GsonBuilder
import de.tum.`in`.tumcampusapp.api.app.Helper
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MvvClient {
    companion object {
        private const val BASE_URL = "https://efa.mvv-muenchen.de/mobile/"
        private var service: MvvApiService? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): MvvApiService {
            if (service == null) {
                service = buildService(context)
            }
            return service!!
        }

        private fun buildService(context: Context): MvvApiService {
            val gson = GsonBuilder()
                    .registerTypeAdapter(DateTime::class.java, MvvDateSerializer())
                    .registerTypeAdapter(MvvStationList::class.java, MvvStationListSerializer())
                    .create()
            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(Helper.getOkHttpClient(context))
                    .build()
                    .create(MvvApiService::class.java)
        }
    }
}