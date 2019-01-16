package de.tum.`in`.tumcampusapp.component.notifications.persistence

enum class NotificationType(val id: Int) {

    CAFETERIA(0),
    CALENDAR(1),
    NEWS(2),
    TRANSPORT(3),
    TUITION_FEES(4),
    GRADES(5);

    companion object {
        private val map = NotificationType.values().associateBy(NotificationType::id)
        fun fromId(id: Long) = map[id.toInt()]
    }

}