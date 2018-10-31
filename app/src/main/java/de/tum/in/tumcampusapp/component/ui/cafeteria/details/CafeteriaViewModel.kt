package de.tum.`in`.tumcampusapp.component.ui.cafeteria.details

import androidx.lifecycle.ViewModel
import android.location.Location
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime

/**
 * ViewModel for cafeterias.
 */
class CafeteriaViewModel(private val localRepository: CafeteriaLocalRepository,
                         private val remoteRepository: CafeteriaRemoteRepository,
                         private val compositeDisposable: CompositeDisposable) : ViewModel() {

    /**
     * Returns a flowable that emits a list of cafeterias from the local repository.
     */
    fun getAllCafeterias(location: Location): Flowable<List<Cafeteria>> =
            localRepository.getAllCafeterias()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { transformCafeteria(it, location) }
                    .defaultIfEmpty(emptyList())

    fun getCafeteriaWithMenus(cafeteriaId: Int): CafeteriaWithMenus {
        return localRepository.getCafeteriaWithMenus(cafeteriaId)
    }

    fun getCafeteriaMenus(id: Int, date: DateTime): Flowable<List<CafeteriaMenu>> {
        return Flowable
                .fromCallable { localRepository.getCafeteriaMenus(id, date) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .defaultIfEmpty(emptyList())
    }

    fun getAllMenuDates(): Flowable<List<DateTime>> {
        return Flowable
                .fromCallable { localRepository.getAllMenuDates() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .defaultIfEmpty(emptyList())
    }


    /**
     * Downloads cafeterias and stores them in the local repository.
     *
     * First checks whether a sync is necessary
     * Then clears current cache
     * Insert new cafeterias
     * Lastly updates last sync
     *
     */
    fun getCafeteriasFromService(force: Boolean): Boolean =
            compositeDisposable.add(Observable.just(1)
                    .filter { localRepository.getLastSync() == null || force }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.io())
                    .doOnNext { localRepository.clear() }
                    .flatMap { remoteRepository.getAllCafeterias() }
                    .doAfterNext { localRepository.updateLastSync() }
                    .subscribe({ cafeteria ->
                        cafeteria.forEach { localRepository.addCafeteria(it) }
                    }, { throwable -> Utils.log(throwable) })
            )

    /**
     * Adds the distance between user and cafeteria to model.
     */
    private fun transformCafeteria(cafeterias: List<Cafeteria>, location: Location): List<Cafeteria> =
            cafeterias.map {
                val results = FloatArray(1)
                Location.distanceBetween(it.latitude, it.longitude, location.latitude, location.longitude, results)
                it.distance = results[0]
                it
            }
}
