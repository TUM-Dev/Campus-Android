package de.tum.`in`.tumcampusapp.models.tumcabe

import de.tum.`in`.tumcampusapp.adapters.SimpleStickyListHeadersAdapter

/**
 * Presents the faculty model that is used in fetching the facultyData from server
 */
data class Curriculum(var curriculum: String?, var category: String?, var name: String?, var url: String?) :
        Comparable<Curriculum>, SimpleStickyListHeadersAdapter.SimpleStickyListItem {

    override fun getHeadName(): String = category!![0].toUpperCase() + category!!.substring(1, category!!.length)

    override fun getHeaderId() = category

    override fun compareTo(other: Curriculum): Int =
            if (category == other.category) {
                name!!.compareTo(other.name!!)
            } else {
                category!!.compareTo(other.category!!)
            }

}