package de.tum.`in`.tumcampusapp.component.ui.news

import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.component.ui.news.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.news.repository.KinoRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * ViewModel for kinos.
 */
class KinoViewModel(private val localRepository: KinoLocalRepository,
                    private val remoteRepository: KinoRemoteRepository,
                    private val compositeDisposable: CompositeDisposable) : ViewModel() {

    /**
     * Get all kinos from database
     */
    fun getAllKinos(): Flowable<List<Kino>> =
            KinoLocalRepository.getAllKinos()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .defaultIfEmpty(emptyList())

    /**
     * Get a kino by its position (id)
     */
    fun getKinoByPosition(position: Int): Flowable<Kino> =
            KinoLocalRepository.getKinoByPosition(position)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    fun getEventByMovieId(movieId: String): Flowable<Event> =
            KinoLocalRepository.getEventByMovieId(movieId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    /**
     * Downloads kinos and stores them in the local repository.
     *
     * First checks whether a sync is necessary
     * Then clears current cache
     * Insert new kinos
     * Lastly updates last sync
     *
     */
    fun getKinosFromService(force: Boolean): Boolean {
        val latestId = KinoLocalRepository.getLatestId() ?: "0"
        return compositeDisposable.add(
                remoteRepository
                        .getAllKinos(latestId)
                        .filter { localRepository.getLastSync() == null || force }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext { localRepository.clear() }
                        .doAfterNext { localRepository.updateLastSync() }
                        .flatMapIterable { it }
                        .filter { it.isFutureMovie() }
                        .subscribe({ localRepository.addKino(it) }, { Utils.log(it) })
        )
    }

    fun getPosition(date: String) = localRepository.getPosition(date)

}