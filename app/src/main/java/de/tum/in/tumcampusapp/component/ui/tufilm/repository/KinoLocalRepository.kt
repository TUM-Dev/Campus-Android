package de.tum.`in`.tumcampusapp.component.ui.tufilm.repository

import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.sync.model.Sync
import io.reactivex.Flowable
import org.joda.time.DateTime
import javax.inject.Inject

class KinoLocalRepository @Inject constructor(
    private val database: TcaDb
) {

    fun getLastSync() = database.syncDao().getSyncSince(Kino::class.java.name, TIME_TO_SYNC)

    fun updateLastSync() = database.syncDao().insert(Sync(Kino::class.java.name, DateTime.now()))

    fun addKino(vararg kino: Kino) = database.kinoDao().insert(*kino)

    fun getAllKinos(): Flowable<List<Kino>> = database.kinoDao().all

    fun getLatestId(): String? = database.kinoDao().latestId

    fun getKinoByPosition(position: Int): Flowable<Kino> = database.kinoDao().getByPosition(position)

    fun getEventByMovieId(movieId: String): Flowable<Event> = database.eventDao().getEventByMovie(movieId)

    fun getPositionByDate(date: String) = database.kinoDao().getPositionByDate(date)

    fun getPositionById(id: String) = database.kinoDao().getPositionById(id)

    companion object {
        private const val TIME_TO_SYNC = 1800
    }
}