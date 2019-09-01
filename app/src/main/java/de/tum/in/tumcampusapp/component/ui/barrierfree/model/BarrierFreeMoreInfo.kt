package de.tum.`in`.tumcampusapp.component.ui.barrierfree.model

import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter

/**
 * The model used to display more infromation in barrier free page
 */
data class BarrierFreeMoreInfo(
    var title: String = "",
    var category: String = "",
    var url: String = ""
) : SimpleStickyListHeadersAdapter.SimpleStickyListItem {

    override fun getHeadName() = category

    override fun getHeaderId() = category
}
