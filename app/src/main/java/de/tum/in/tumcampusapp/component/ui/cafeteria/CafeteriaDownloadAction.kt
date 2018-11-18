package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.details.CafeteriaViewModel
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository
import de.tum.`in`.tumcampusapp.utils.tcaDb
import de.tum.`in`.tumcampusapp.utils.tumCabeClient
import io.reactivex.disposables.CompositeDisposable

class CafeteriaDownloadAction(private val context: Context, disposable: CompositeDisposable) :
        (CacheControl) -> Unit {

    private val cafeteriaViewModel: CafeteriaViewModel
    init {
        CafeteriaRemoteRepository.tumCabeClient = context.tumCabeClient
        CafeteriaLocalRepository.db = context.tcaDb
        cafeteriaViewModel = CafeteriaViewModel(
                CafeteriaLocalRepository, CafeteriaRemoteRepository, disposable
        )
    }

    override fun invoke(cacheBehaviour: CacheControl) {
        CafeteriaMenuManager(context).downloadMenus(cacheBehaviour)
        cafeteriaViewModel.getCafeteriasFromService(cacheBehaviour)
    }
}
