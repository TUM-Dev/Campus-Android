package de.tum.`in`.tumcampusapp.component.ui.news

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl

class NewsDownloadAction(context: Context) :
        (CacheControl) -> Unit {

    private val newsController = NewsController(context)
    override fun invoke(cacheBehaviour: CacheControl) {
        newsController.downloadFromExternal(cacheBehaviour)
    }
}