package de.tum.`in`.tumcampusapp.component.other.generic.fragment

import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient

abstract class FragmentForAccessingTumCabe<T>(
        layoutId: Int
) : BaseFragment<T>(layoutId) {

    protected val apiClient: TUMCabeClient by lazy {
        TUMCabeClient.getInstance(requireContext())
    }

}
