package de.tum.`in`.tumcampusapp.component.ui.overview

import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter

data class NotificationItemForStickyList(
        val notificationString : String,
        val notificationSource : String,
        ): SimpleStickyListHeadersAdapter.SimpleStickyListItem {

    override fun getHeadName(): String {
        return notificationSource
    }

    override fun getHeaderId(): String {
        return notificationSource
    }


}