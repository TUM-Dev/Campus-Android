package de.tum.`in`.tumcampusapp.component.other.generic.fragment

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient

abstract class FragmentForAccessingTumCabe<T>(
    @LayoutRes layoutId: Int,
    @StringRes titleResId: Int
) : BaseFragment<T>(layoutId, titleResId) {

    protected val apiClient: TUMCabeClient by lazy {
        TUMCabeClient.getInstance(requireContext())
    }
}
