package de.tum.`in`.tumcampusapp.component.ui.news.repository

import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.sync.model.Sync
import io.reactivex.Flowable
import org.joda.time.DateTime

object KinoLocalRepository {

    private const val TIME_TO_SYNC = 1800

    lateinit var db: TcaDb

    fun getLastSync() = db.syncDao().getSyncSince(Kino::class.java.name, TIME_TO_SYNC)

    fun updateLastSync() = db.syncDao().insert(Sync(Kino::class.java.name, DateTime.now()))

    fun addKino(kino: Kino) = db.kinoDao().insert(kino)

    fun getAllKinos(): Flowable<List<Kino>> = db.kinoDao().all

    fun getLatestId(): String? = db.kinoDao().latestId

    fun getKinoByPosition(position: Int): Flowable<Kino> = db.kinoDao().getByPosition(position)

    fun clear() = db.kinoDao().cleanUp()

    fun getPosition(date: String) = db.kinoDao().getPosition(date)

}