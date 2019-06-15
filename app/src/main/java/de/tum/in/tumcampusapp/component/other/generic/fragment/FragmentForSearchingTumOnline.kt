package de.tum.`in`.tumcampusapp.component.other.generic.fragment

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient

/**
 * @param T The type of object that is loaded from the TUMonline API
 */
abstract class FragmentForSearchingTumOnline<T>(
    @LayoutRes layoutId: Int,
    @StringRes titleResId: Int,
    authority: String,
    minLength: Int
) : FragmentForSearching<T>(layoutId, titleResId, authority, minLength) {

    protected val apiClient: TUMOnlineClient by lazy {
        TUMOnlineClient.getInstance(requireContext())
    }

}
