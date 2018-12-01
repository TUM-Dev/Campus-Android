package de.tum.`in`.tumcampusapp.component.ui.news.repository

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.ErrorHelper
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class TopNewsRemoteRepository @Inject constructor(
        private val context: Context,
        private val tumCabeClient: TUMCabeClient
) {

    private val compositeDisposable = CompositeDisposable()

    fun fetchNewsAlert() {
        compositeDisposable += tumCabeClient.newsAlert
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .subscribe({ newsAlert ->
                    Utils.setSetting(context, Const.NEWS_ALERT_IMAGE, newsAlert.url)
                    Utils.setSetting(context, Const.NEWS_ALERT_LINK, newsAlert.link)

                    val oldShowUntil = Utils.getSetting(context, Const.NEWS_ALERT_SHOW_UNTIL, "")
                    val oldImage = Utils.getSetting(context, Const.NEWS_ALERT_IMAGE, "");

                    // there is a NewsAlert update if the image link or the date changed
                    // --> Card should be displayed again
                    val update = oldShowUntil != newsAlert.displayUntil || oldImage != newsAlert.url
                    if (update) {
                        Utils.setSetting(context, CardManager.SHOW_TOP_NEWS, true)
                    }
                    Utils.setSetting(context, Const.NEWS_ALERT_SHOW_UNTIL, newsAlert.displayUntil)
                }, ErrorHelper::logAndIgnore)
    }

    fun cancel() {
        compositeDisposable.dispose()
    }

}
