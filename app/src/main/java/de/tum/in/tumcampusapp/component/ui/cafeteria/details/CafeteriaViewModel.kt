package de.tum.`in`.tumcampusapp.component.ui.cafeteria.details

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization.Label
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.LabelLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.LabelRemoteRepository
import de.tum.`in`.tumcampusapp.utils.LocationHelper.calculateDistanceToCafeteria
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import javax.inject.Inject

class CafeteriaViewModel @Inject constructor(
        private val localRepository: CafeteriaLocalRepository,
        private val remoteRepository: CafeteriaRemoteRepository,
        private val localLabelRepository: LabelLocalRepository,
        private val remoteLabelRepository: LabelRemoteRepository
) : ViewModel() {

    private val _cafeterias = MutableLiveData<List<Cafeteria>>()
    val cafeterias: LiveData<List<Cafeteria>> = _cafeterias

    private val _selectedCafeteria = MutableLiveData<Cafeteria>()
    val selectedCafeteria: LiveData<Cafeteria> = _selectedCafeteria

    private val _cafeteriaMenus = MutableLiveData<List<CafeteriaMenu>>()
    val cafeteriaMenus: LiveData<List<CafeteriaMenu>> = _cafeteriaMenus

    private val _menuDates = MutableLiveData<List<DateTime>>()
    val menuDates: LiveData<List<DateTime>> = _menuDates

    private val _labels = MutableLiveData<List<Label>>()
    val labels: LiveData<List<Label>> = _labels

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> = _error

    private val compositeDisposable = CompositeDisposable()

    /**
     * Updates the currently selected [Cafeteria] and posts a new value to [selectedCafeteria].
     *
     * @param cafeteria The newly selected [Cafeteria]
     */
    fun updateSelectedCafeteria(cafeteria: Cafeteria) {
        _selectedCafeteria.postValue(cafeteria)
    }

    /**
     * Fetches all [Cafeteria]s around the provided [Location] from the database and posts the
     * results to [cafeterias].
     *
     * @param location The current [Location]
     */
    fun fetchCafeterias(location: Location) {
        compositeDisposable += localRepository.getAllCafeterias()
                .map { transformCafeteria(it, location) }
                .subscribeOn(Schedulers.io())
                .defaultIfEmpty(emptyList())
                .doOnError { _error.postValue(true) }
                .doOnNext { _error.postValue(it.isEmpty()) }
                .subscribe(_cafeterias::postValue, Utils::log)
    }

    /**
     * Fetches all menu dates from the database and posts them to [menuDates].
     */
    fun fetchMenuDates() {
        compositeDisposable += Flowable.fromCallable { localRepository.getAllMenuDates() }
                .subscribeOn(Schedulers.io())
                .defaultIfEmpty(emptyList())
                .subscribe(_menuDates::postValue, Utils::log)
    }

    fun fetchCafeteriaMenus(context: Context?, cafeteriaId: Int, date: DateTime) {
        if (localRepository.hasNoMenusFor(cafeteriaId, date)) {
            if (context != null) {
                downloadRemoteCafeteriaMenus(cafeteriaId, date, context)
            } else {
                Utils.logWithTag(this::class.java.name, "Cannot download remote cafeteria menus, because the provided context was 'null'.")
            }
        }

        remoteLabelRepository.downloadLabels()
        fetchLocalCafeteriaMenus(cafeteriaId, date)
    }

    fun initializeMenuDatesWithCurrentDate() {
        _menuDates.value = listOf(DateTime.now())
    }

    private fun fetchLocalCafeteriaMenus(cafeteriaId: Int, date: DateTime) {
        compositeDisposable += Flowable.fromCallable { localRepository.getCafeteriaMenus(cafeteriaId, date) }
                .subscribeOn(Schedulers.io())
                .defaultIfEmpty(emptyList())
                .subscribe { _cafeteriaMenus.postValue(it) }
    }

    private fun downloadRemoteCafeteriaMenus(cafeteriaId: Int, date: DateTime, context: Context) {
        remoteRepository.downloadRemoteMenus(cafeteriaId, date, context)
    }

    /**
     * Adds the distance between user and cafeteria to model.
     */
    private fun transformCafeteria(
        cafeterias: List<Cafeteria>,
        location: Location
    ): List<Cafeteria> {
        return cafeterias.map {
            it.distance = calculateDistanceToCafeteria(it, location)
            it
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
