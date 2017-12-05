package de.tum.`in`.tumcampusapp.database.repository

import android.content.Context
import com.google.gson.GsonBuilder
import de.tum.`in`.tumcampusapp.api.Helper
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class NetAPIImpl(val context: Context) : NetAPI {

    override fun getCafeterias() = service.getCafeterias()

    private val service: NetAPI

    init {
        val retro = Retrofit.Builder().baseUrl("https://tumcabe.in.tum.de/Api/")
                .addConverterFactory(
                        GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(Helper.getOkClient(context)).build()
        service = retro.create(NetAPI::class.java)
    }
}
