package de.tum.`in`.tumcampusapp.component.ui.tufilm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * ViewModel for kinos.
 */
class KinoViewModel @Inject constructor(
        private val localRepository: KinoLocalRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _kinos = MutableLiveData<List<Kino>>()
    val kinos: LiveData<List<Kino>> = _kinos

    private val _error = MutableLiveData<Int>()
    val error: LiveData<Int> = _error

    init {
        fetchKinos()
    }

    private fun fetchKinos() {
        compositeDisposable += getAllKinos()
                .subscribe(_kinos::postValue) {
                    _error.postValue(R.string.error_something_wrong)
                }
    }

    /**
     * Get all kinos from database
     */
    fun getAllKinos(): Flowable<List<Kino>> =
            localRepository.getAllKinos()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .defaultIfEmpty(emptyList())

    fun getPositionByDate(date: String) = localRepository.getPositionByDate(date)

    fun getPositionById(id: String) = localRepository.getPositionById(id)

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}