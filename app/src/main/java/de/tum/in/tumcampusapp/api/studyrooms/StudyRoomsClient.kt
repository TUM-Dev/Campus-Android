package de.tum.`in`.tumcampusapp.api.studyrooms

import android.content.Context
import com.google.gson.GsonBuilder
import de.tum.`in`.tumcampusapp.api.app.ChaosMonkeyInterceptor
import de.tum.`in`.tumcampusapp.api.app.ConnectivityInterceptor
import de.tum.`in`.tumcampusapp.api.app.TumHttpLoggingInterceptor
import de.tum.`in`.tumcampusapp.utils.Utils
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StudyRoomsClient {

    companion object {

        private const val BASE_URL = "https://www.devapp.it.tum.de/iris/"
        private const val TAG = "STUDY_ROOMS_API_CALL"

        private var apiService: StudyRoomsAPIService? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): StudyRoomsAPIService {
            if (apiService == null) {
                apiService = buildAPIService(context)
            }

            return apiService!!
        }

        private fun buildAPIService(context: Context): StudyRoomsAPIService {
            val httpLoggingInterceptor = TumHttpLoggingInterceptor {
                message -> Utils.logwithTag(TAG, message)
            }

            val client = OkHttpClient.Builder()
                    .addInterceptor(ChaosMonkeyInterceptor())
                    .addInterceptor(ConnectivityInterceptor(context))
                    .addNetworkInterceptor(httpLoggingInterceptor)
                    .build()

            val gson = GsonBuilder()
                    .registerTypeAdapter(DateTime::class.java, DateTimeSerializer())
                    .create()

            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(StudyRoomsAPIService::class.java)
        }

    }

}