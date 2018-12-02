package de.tum.`in`.tumcampusapp.component.ui.cafeteria.details

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime

/**
 * ViewModel for cafeterias.
 */
class CafeteriaViewModel(
        private val localRepository: CafeteriaLocalRepository
) : ViewModel() {

    /**
     * Returns a flowable that emits a list of cafeterias from the local repository.
     */
    fun getAllCafeterias(location: Location): Flowable<List<Cafeteria>> = // TODO: LiveData
            localRepository.getAllCafeterias()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { transformCafeteria(it, location) }
                    .defaultIfEmpty(emptyList())

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
     * Adds the distance between user and cafeteria to model.
     */
    private fun transformCafeteria(cafeterias: List<Cafeteria>, location: Location): List<Cafeteria> =
            cafeterias.map {
                val results = FloatArray(1)
                Location.distanceBetween(it.latitude, it.longitude, location.latitude, location.longitude, results)
                it.distance = results[0]
                it
            }

    class Factory(
            private val localRepository: CafeteriaLocalRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CafeteriaViewModel(localRepository) as T
        }

    }

}
