package de.tum.`in`.tumcampusapp.models.tumcabe

import de.tum.`in`.tumcampusapp.adapters.SimpleStickyListHeadersAdapter

/**
 * The model used to display more infromation in barrier free page
 */
data class BarrierfreeMoreInfo(var title: String = "",
                               var category: String = "",
                               var url: String = "") : SimpleStickyListHeadersAdapter.SimpleStickyListItem {

    override fun getHeadName() = category

    override fun getHeaderId() = category
}
