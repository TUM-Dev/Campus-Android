package de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository

import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LabelRemoteRepository @Inject constructor(private val tumCabeClient: TUMCabeClient, private val localRepository: LabelLocalRepository, private val db: TcaDb) {

    fun fetchLabels() {
        if (tumCabeClient != null) {
            Observable.just(1)
                    .subscribeOn(Schedulers.io())
                    .doOnNext { localRepository.clear() }
                    .flatMap { tumCabeClient.labels }
                    .doAfterNext { localRepository.updateLastSync() }
                    .subscribe(localRepository::addLabels, Utils::log)
        }
    }
}