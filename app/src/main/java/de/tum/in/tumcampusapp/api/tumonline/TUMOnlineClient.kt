package de.tum.`in`.tumcampusapp.api.tumonline

import android.content.Context
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import de.tum.`in`.tumcampusapp.api.app.Helper
import retrofit2.Retrofit

class TUMOnlineClient {

    companion object {

        private const val BASE_URL = "https://campus.tum.de/tumonline/"

        private var apiService: TUMOnlineAPIService? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): TUMOnlineAPIService {
            if (apiService == null) {
                apiService = buildAPIService(context)
            }

            return apiService!!
        }

        private fun buildAPIService(context: Context): TUMOnlineAPIService {
            val client = Helper.getOkHttpClient(context)
                    .newBuilder()
                    .addInterceptor(TUMOnlineInterceptor(context))
                    .build()

            // TODO: Add TypeConverter for date strings
            val tikXml = TikXml.Builder()
                    .exceptionOnUnreadXml(false)
                    .build()
            val xmlConverterFactory = TikXmlConverterFactory.create(tikXml)

            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(xmlConverterFactory)
                    .build()
                    .create(TUMOnlineAPIService::class.java)
        }

    }

}