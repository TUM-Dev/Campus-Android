package de.tum.`in`.tumcampusapp.component.other.generic.fragment

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient

abstract class FragmentForAccessingTumOnline<T>(
    @LayoutRes layoutId: Int,
    @StringRes titleResId: Int
) : BaseFragment<T>(layoutId, titleResId) {

    protected val apiClient: TUMOnlineClient by lazy {
        TUMOnlineClient.getInstance(requireContext())
    }
}
