package de.tum.`in`.tumcampusapp.component.ui.tufilm

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.ui.news.KinoViewModel
import de.tum.`in`.tumcampusapp.component.ui.news.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.news.repository.KinoRemoteRepository
import de.tum.`in`.tumcampusapp.utils.tcaDb
import de.tum.`in`.tumcampusapp.utils.tumCabeClient
import io.reactivex.disposables.CompositeDisposable

class FilmDownloadAction(context: Context, disposable: CompositeDisposable) :
        (CacheControl) -> Unit {

    private val kinoViewModel: KinoViewModel
    init {
        KinoLocalRepository.db = context.tcaDb
        KinoRemoteRepository.tumCabeClient = context.tumCabeClient
        kinoViewModel = KinoViewModel(KinoLocalRepository, KinoRemoteRepository, disposable)
    }

    override fun invoke(cacheBehaviour: CacheControl) {
        kinoViewModel.getKinosFromService(cacheBehaviour)
    }
}
