package de.tum.`in`.tumcampusapp.models.tumcabe

import de.tum.`in`.tumcampusapp.adapters.SimpleStickyListHeadersAdapter

/**
 * The model used to display contact infromation in barrier free page
 */
data class BarrierfreeContact(val name: String?, val telephone: String, val email: String, val faculty: String, val tumID: String) :
        SimpleStickyListHeadersAdapter.SimpleStickyListItem {

    val isValid: Boolean
        get() = name != null && name != "null"

    val isHavingTumID: Boolean
        get() = !(tumID == "null" || tumID == "")


    override fun getHeadName() = faculty

    override fun getHeaderId() = faculty
}
