package de.tum.`in`.tumcampusapp.component.other.generic.activity

import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient

/**
 * Generic class which handles all basic tasks to communicate with TUMOnline and
 * provides a [android.support.v7.widget.SearchView] for searching the data.
 * It implements the TUMOnlineRequestFetchListener in order to receive data from
 * TUMOnline and implements a rich user feedback with error progress and token
 * related layouts. Generic class parameter specifies the type of data returned by TumOnline.
 *
 * @param T The type of object that is loaded from the TUMonline API
 */
@Deprecated("Use BaseActivity and a suitable BaseFragment class")
abstract class ActivityForSearchingTumOnline<T>(
        layoutId: Int,
        auth: String,
        minLen: Int
) : ActivityForSearching<T>(layoutId, auth, minLen) {

    protected val apiClient: TUMOnlineClient by lazy {
        TUMOnlineClient.getInstance(this)
    }

}
