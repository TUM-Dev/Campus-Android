package de.tum.`in`.tumcampusapp.component.ui.barrierfree.model

import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter

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

    val hasTumID: Boolean
        get() = !(tumID == "null" || tumID == "")


    override fun getHeadName() = faculty

    override fun getHeaderId() = faculty
}
