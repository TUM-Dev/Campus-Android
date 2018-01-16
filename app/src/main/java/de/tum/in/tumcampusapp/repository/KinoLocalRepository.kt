package de.tum.`in`.tumcampusapp.repository

import de.tum.`in`.tumcampusapp.auxiliary.Utils
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.models.dbEntities.Sync
import de.tum.`in`.tumcampusapp.models.tumcabe.Kino
import io.reactivex.Flowable
import io.reactivex.Maybe
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object KinoLocalRepository {

    private const val TIME_TO_SYNC = 1800

    lateinit var db: TcaDb

    fun getLastSync() = db.syncDao().getSyncSince(Kino::class.java.name, TIME_TO_SYNC)

    fun updateLastSync() = db.syncDao().insert(Sync(Kino::class.java.name, Utils.getDateTimeString(Date())))

    fun addKino(kino: Kino) = db.kinoDao().insert(kino)

    fun getAllKinos(): Flowable<List<Kino>> = db.kinoDao().all

    fun getLastId(): Maybe<String> = db.kinoDao().lastId

    fun getKinoByPosition(position: Int): Flowable<Kino> = db.kinoDao().getByPosition(position)

    fun clear() = db.kinoDao().cleanUp()

}