package de.tum.`in`.tumcampusapp.component.ui.news;

import android.arch.lifecycle.ViewModel
import android.content.Context
import de.tum.`in`.tumcampusapp.component.ui.news.repository.TopNewsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Observable
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
        fun getNewsAlertFromService(context: Context): Boolean =
        compositeDisposable.add(Observable.just(1)
        .subscribeOn(Schedulers.computation())
        .flatMap { remoteRepository.getNewsAlert() }.observeOn(Schedulers.io())
        .doOnError { Utils.log(it.message) }
        .subscribe({ t ->
                        Utils.setSetting(context, Const.NEWS_ALERT_IMAGE, t.url)
                        Utils.setSetting(context, Const.NEWS_ALERT_LINK, t.link)

                        val oldShowUntil = Utils.getSetting(context, Const.NEWS_ALERT_SHOW_UNTIL, "")
                        val oldImage = Utils.getSetting(context, Const.NEWS_ALERT_IMAGE, "");

                        // there is a NewsAlert update if the image link or the date changed --> Card should be displayed again
                        val update = !oldShowUntil.equals(t.displayUntil) || !oldImage.equals(t.url)
                        if(update){
                            Utils.setSetting(context, CardManager.SHOW_TOP_NEWS, true)
                        }
                        Utils.setSetting(context, Const.NEWS_ALERT_SHOW_UNTIL, t.displayUntil)
                })
        )}

