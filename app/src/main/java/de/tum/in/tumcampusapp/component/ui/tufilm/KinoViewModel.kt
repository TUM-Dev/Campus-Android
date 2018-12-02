package de.tum.`in`.tumcampusapp.component.ui.tufilm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * ViewModel for kinos.
 */
class KinoViewModel(
        private val localRepository: KinoLocalRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _kinos = MutableLiveData<List<Kino>>()
    val kinos: LiveData<List<Kino>> = _kinos

    private val _error = MutableLiveData<Int>()
    val error: LiveData<Int> = _error

    init {
        fetchAllKinos()
    }

    private fun fetchAllKinos() {
        compositeDisposable += localRepository.getAllKinos()
                .subscribeOn(Schedulers.io())
                .defaultIfEmpty(emptyList())
                .doOnError(Utils::log)
                .subscribe(_kinos::postValue) {
                    _error.postValue(R.string.error_something_wrong)
                }
    }

    /**
     * Get all kinos from database
     */
    fun getAllKinos(): Flowable<List<Kino>> = // TODO(thellmund) Remove once test is fixed
            localRepository.getAllKinos()
                    .subscribeOn(Schedulers.io())
                    .defaultIfEmpty(emptyList())

    fun getPositionByDate(date: String) = localRepository.getPositionByDate(date)

    fun getPositionById(id: String) = localRepository.getPositionById(id)

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    class Factory(
            private val localRepository: KinoLocalRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return KinoViewModel(localRepository) as T
        }

    }

}
