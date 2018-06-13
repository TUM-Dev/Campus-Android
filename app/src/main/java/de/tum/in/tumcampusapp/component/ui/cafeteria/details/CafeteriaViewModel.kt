package de.tum.`in`.tumcampusapp.component.ui.cafeteria.details

import android.arch.lifecycle.ViewModel
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
        return CafeteriaWithMenus(cafeteriaId).apply {
            name = getCafeteriaNameFromId(id).blockingFirst()
            menuDates = getAllMenuDates().blockingFirst()
            menus = getCafeteriaMenus(id, nextMenuDate).blockingFirst()
        }
    }

    fun getCafeteriaNameFromId(id: Int): Flowable<String> =
            localRepository.getCafeteria(id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { it.name }

    fun getCafeteriaMenus(id: Int, date: String): Flowable<List<CafeteriaMenu>> =
            localRepository.getCafeteriaMenus(id,date)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .defaultIfEmpty(emptyList())

    fun getAllMenuDates():Flowable<List<String>> =
            localRepository.getAllMenuDates()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .defaultIfEmpty(emptyList())


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
                    .doOnNext { localRepository.clear() }
                    .flatMap { remoteRepository.getAllCafeterias() }.observeOn(Schedulers.io())
                    .doAfterNext { localRepository.updateLastSync() }
                    .doOnError { Utils.log(it.message) }
                    .subscribe({ t -> t.forEach { localRepository.addCafeteria(it) } })
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
