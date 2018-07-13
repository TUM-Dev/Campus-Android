package de.tum.`in`.tumcampusapp.component.ui.news;

import android.arch.lifecycle.ViewModel
import android.content.Context
import de.tum.`in`.tumcampusapp.component.ui.news.repository.TopNewsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * ViewModel for TopNews/NewsAlert.
 */
class TopNewsViewModel(private val remoteRepository: TopNewsRemoteRepository,
                       private val compositeDisposable: CompositeDisposable): ViewModel() {

    /**
     * Downloads the NewsAlert and stores it in the sharedPreferences
     */
    fun getNewsAlertFromService(context: Context): Boolean {
        return compositeDisposable.add(
                remoteRepository
                        .getNewsAlert()
                        .subscribeOn(Schedulers.computation())
                        .observeOn(Schedulers.io())
                        .subscribe({ newsAlert ->
                            // TODO: Store NewsAlerts in Room, not SharedPreferences!
                            Utils.setSetting(context, Const.NEWS_ALERT_IMAGE, newsAlert.url)
                            Utils.setSetting(context, Const.NEWS_ALERT_LINK, newsAlert.link)

                            val oldShowUntil = Utils.getSetting(context, Const.NEWS_ALERT_SHOW_UNTIL, "")
                            val oldImage = Utils.getSetting(context, Const.NEWS_ALERT_IMAGE, "");

                            // There is a NewsAlert update if the image link or the date changed
                            // --> Card should be displayed again
                            val update = oldShowUntil != newsAlert.displayUntil || oldImage != newsAlert.url
                            if (update) {
                                Utils.setSetting(context, CardManager.SHOW_TOP_NEWS, true)
                            }
                            Utils.setSetting(context, Const.NEWS_ALERT_SHOW_UNTIL, newsAlert.displayUntil)
                        }, { t -> Utils.log(t) })
        )
    }
}

