package de.tum.`in`.tumcampusapp.component.ui.tufilm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoRemoteRepository
import de.tum.`in`.tumcampusapp.utils.Utils
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
        private val localRepository: KinoLocalRepository,
        private val remoteRepository: KinoRemoteRepository
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

    /**
     * Downloads kinos and stores them in the local repository.
     *
     * First checks whether a sync is necessary
     * Then clears current cache
     * Insert new kinos
     * Lastly updates last sync
     *
     */
    fun getKinosFromService(force: Boolean) {
        val latestId = localRepository.getLatestId() ?: "0"
        compositeDisposable += remoteRepository
                        .getAllKinos(latestId)
                        .filter { localRepository.getLastSync() == null || force }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doAfterNext { localRepository.updateLastSync() }
                        .flatMapIterable { it }
                        .subscribe({ localRepository.addKino(it) }, { Utils.log("Added Kino: $it") })
    }

    fun getPositionByDate(date: String) = localRepository.getPositionByDate(date)

    fun getPositionById(id: String) = localRepository.getPositionById(id)

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}