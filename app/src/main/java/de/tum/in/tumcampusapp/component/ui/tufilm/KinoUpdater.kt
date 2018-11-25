package de.tum.`in`.tumcampusapp.component.ui.tufilm

import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoRemoteRepository
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class KinoUpdater @Inject constructor(
        private val localRepository: KinoLocalRepository,
        private val remoteRepository: KinoRemoteRepository
) {

    fun fetchAndStoreKinos(force: Boolean): Disposable {
        val latestId = localRepository.getLatestId() ?: "0"
        return remoteRepository
                .getAllKinos(latestId)
                .filter { localRepository.getLastSync() == null || force }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterNext { localRepository.updateLastSync() }
                .flatMapIterable { it }
                .subscribe({ localRepository.addKino(it) }, { Utils.log("Added Kino: $it") })
    }

}
