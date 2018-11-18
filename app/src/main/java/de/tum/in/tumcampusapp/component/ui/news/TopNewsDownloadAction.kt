package de.tum.`in`.tumcampusapp.component.ui.news

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.ui.news.repository.TopNewsRemoteRepository
import de.tum.`in`.tumcampusapp.utils.tumCabeClient
import io.reactivex.disposables.CompositeDisposable

class TopNewsDownloadAction(private val context: Context, disposable: CompositeDisposable) :
        (CacheControl) -> Unit {

    private val topNewsViewModel: TopNewsViewModel
    init {
        TopNewsRemoteRepository.tumCabeClient = context.tumCabeClient
        topNewsViewModel = TopNewsViewModel(TopNewsRemoteRepository, disposable)
    }

    override fun invoke(cacheBehaviour: CacheControl) {
        topNewsViewModel.getNewsAlertFromService(context)
    }
}