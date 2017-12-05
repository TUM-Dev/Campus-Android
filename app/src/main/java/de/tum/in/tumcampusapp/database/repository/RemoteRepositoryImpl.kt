package de.tum.`in`.tumcampusapp.database.repository

import android.content.Context
import de.tum.`in`.tumcampusapp.models.cafeteria.Cafeteria
import io.reactivex.Observable

class RemoteRepositoryImpl(private val netAPI: NetAPI) : RemoteRepository {
    override fun getAllCafeterias(): Observable<List<Cafeteria>> {
        return netAPI.getCafeterias()
    }

    companion object {
        private var instance: RemoteRepositoryImpl? = null
        @JvmStatic
        fun getInstance(context: Context): RemoteRepositoryImpl {
            if (instance == null)
                instance = RemoteRepositoryImpl(NetAPIImpl(context))
            return instance!!
        }
    }
}