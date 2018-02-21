package de.tum.`in`.tumcampusapp.models.tumcabe

import de.tum.`in`.tumcampusapp.adapters.SimpleStickyListHeadersAdapter

/**
 * The model used to display contact infromation in barrier free page
 */
data class BarrierfreeContact(var name: String = "",
                              var telephone: String = "",
                              var email: String = "",
                              var faculty: String = "",
                              var tumID: String = "") :
        SimpleStickyListHeadersAdapter.SimpleStickyListItem {

    val isValid: Boolean
        get() = name != ""

    val isHavingTumID: Boolean
        get() = !(tumID == "null" || tumID == "")


    override fun getHeadName() = faculty

    override fun getHeaderId() = faculty
}
