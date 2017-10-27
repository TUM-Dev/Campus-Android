package de.tum.`in`.tumcampusapp.models.tumcabe

import de.tum.`in`.tumcampusapp.adapters.SimpleStickyListHeadersAdapter

/**
 * The model used to display more infromation in barrier free page
 */
data class BarrierfreeMoreInfo(val title: String, val category: String, val url: String) : SimpleStickyListHeadersAdapter.SimpleStickyListItem {

    override fun getHeadName() = category

    override fun getHeaderId() = category
}
