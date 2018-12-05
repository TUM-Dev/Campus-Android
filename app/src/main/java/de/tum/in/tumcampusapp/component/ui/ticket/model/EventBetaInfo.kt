package de.tum.`in`.tumcampusapp.component.ui.ticket.model

class EventBetaInfo() : EventItem {

    override fun equals(other: Any?): Boolean {
        return (other is EventBetaInfo)
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun getIdForComparison(): Int {
        return -1
    }

}