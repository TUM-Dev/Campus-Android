package de.tum.`in`.tumcampusapp.component.ui.tufilm.repository

import android.annotation.SuppressLint
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class KinoRemoteRepository @Inject constructor(
    private val tumCabeClient: TUMCabeClient,
    private val localRepository: KinoLocalRepository
) {

    /**
     * Downloads kinos and stores them in the local repository.
     *
     * First checks whether a sync is necessary
     * Then clears current cache
     * Insert new kinos
     * Lastly updates last sync
     *
     */
    @SuppressLint("CheckResult")
    fun fetchKinos(force: Boolean) {
        val latestId = localRepository.getLatestId() ?: "0"
        tumCabeClient.getKinos(latestId)
                .filter { localRepository.getLastSync() == null || force }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterNext { localRepository.updateLastSync() }
                .map { it.toTypedArray() }
                .subscribe(localRepository::addKino, Utils::log)
    }
}
