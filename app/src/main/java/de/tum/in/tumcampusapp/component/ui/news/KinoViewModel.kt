package de.tum.`in`.tumcampusapp.component.ui.news

import android.arch.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.component.ui.news.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.news.repository.KinoRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

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

    fun getLastId(): Maybe<String> =
            KinoLocalRepository.getLastId()
                    .defaultIfEmpty("")
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
    fun getKinosFromService(force: Boolean): Boolean =
            compositeDisposable.add(Observable.just("")
                    .filter { localRepository.getLastSync() == null || force }
                    .subscribeOn(Schedulers.computation())
                    .doOnNext { localRepository.clear() }
                    .flatMap { remoteRepository.getAllKinos(getLastId().blockingGet()) }.observeOn(Schedulers.io())
                    .doAfterNext { localRepository.updateLastSync() }
                    .doOnError { Utils.log(it) }
                    .map { transformKino(it) }
                    .subscribe({ it.forEach {
                        if(isMovieInFuture(it))
                            localRepository.addKino(it)
                    } })
            )

    private fun isMovieInFuture(kino: Kino): Boolean = Date().before(kino.date)

    fun getPosition(date: String) = localRepository.getPosition(date)

    /**
     * Sets fields that might be null to prevent excpetions when inserting a kino object into the database
     */
    private fun transformKino(kinos: List<Kino>): List<Kino> =
            kinos.map {
                it.trailer ?: ""
                it
            }

}