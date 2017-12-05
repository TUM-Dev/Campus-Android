package de.tum.`in`.tumcampusapp.database.repository

import android.content.Context
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.models.cafeteria.Cafeteria
import io.reactivex.Flowable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class LocalRepositoryImpl(val db: TcaDb, private val executor: Executor) : LocalRepository {
    override fun getAllCafeterias(): Flowable<List<Cafeteria>> {
        return db.cafeteriaDao().all
    }

    override fun getCafeteria(id: Int): Flowable<Cafeteria> {
        return db.cafeteriaDao().getById(id)
    }

    override fun addCafeteria(cafeteria: Cafeteria) {
        executor.execute { db.cafeteriaDao().insert(cafeteria) }
    }

    companion object {
        private var instance: LocalRepositoryImpl? = null
        @JvmStatic
        fun getInstance(context: Context): LocalRepositoryImpl {
            if (instance == null)
                instance = LocalRepositoryImpl(TcaDb.getInstance(context), Executors.newSingleThreadExecutor())
            return instance!!
        }
    }

}
