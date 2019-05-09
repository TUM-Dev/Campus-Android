package de.tum.`in`.tumcampusapp.component.other.generic.fragment

import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient

abstract class FragmentForAccessingTumOnline<T>(
        layoutId: Int
) : BaseFragment<T>(layoutId) {

    protected val apiClient: TUMOnlineClient by lazy {
        TUMOnlineClient.getInstance(requireContext())
    }

}
