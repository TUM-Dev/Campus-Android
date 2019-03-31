package de.tum.`in`.tumcampusapp.component.ui.openinghour

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

interface OpeningApi {
    companion object {
        private const val BASE_URL = "https://app.tum.de/"

        fun buildOpeningHours(): OpeningApi {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(
                            RxJava2CallAdapterFactory.create())
                    .addConverterFactory(
                            GsonConverterFactory.create())
                    .baseUrl(BASE_URL)
                    .build()

            return retrofit.create(OpeningApi::class.java)
        }
    }
}