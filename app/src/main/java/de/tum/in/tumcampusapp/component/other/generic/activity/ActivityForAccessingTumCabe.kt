package de.tum.`in`.tumcampusapp.component.other.generic.activity

import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient

/**
 * This Activity can be extended by concrete Activities that access information from TUM Cabe. It
 * includes methods for fetching content (both via [TUMOnlineClient] and from the local
 * cache, and implements error and retry handling.
 */
abstract class ActivityForAccessingTumCabe<T>(layoutId: Int) : ProgressActivity<T>(layoutId) {

    protected val apiClient: TUMCabeClient by lazy {
        TUMCabeClient.getInstance(this)
    }

}