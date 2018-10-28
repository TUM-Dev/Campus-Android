package de.tum.`in`.tumcampusapp.component.other.generic.activity

import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient

/**
 * This Activity can be extended by concrete Activities that access information from TUMonline. It
 * includes methods for fetching content (both via [TUMOnlineClient] and from the local
 * cache, and implements error and retry handling.
 *
 * @param T The type of object that is loaded from the TUMonline API
 */
abstract class ActivityForAccessingTumOnline<T>(layoutId: Int) : ProgressActivity<T>(layoutId) {

    protected val apiClient: TUMOnlineClient by lazy {
        TUMOnlineClient.getInstance(this)
    }

}
