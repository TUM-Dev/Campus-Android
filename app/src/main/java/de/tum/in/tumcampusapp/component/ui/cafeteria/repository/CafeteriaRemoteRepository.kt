package de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository

import android.annotation.SuppressLint
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class CafeteriaRemoteRepository(
        private val tumCabeClient: TUMCabeClient,
        private val localRepository: CafeteriaLocalRepository
) {

    /**
     * Downloads cafeterias and stores them in the local repository.
     *
     * First checks whether a sync is necessary
     * Then clears current cache
     * Insert new cafeterias
     * Lastly updates last sync
     *
     */
    @SuppressLint("CheckResult")
    fun fetchCafeterias(force: Boolean) {
        Observable.fromCallable { localRepository.getLastSync() == null || force }
                .doOnNext { localRepository.clear() }
                .doAfterNext { localRepository.updateLastSync() }
                .flatMap { tumCabeClient.cafeterias }
                .subscribeOn(Schedulers.io())
                .subscribe(localRepository::addCafeterias, Utils::log)
    }

}
