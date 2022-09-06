package de.tum.`in`.tumcampusapp.component.ui.search

import de.tum.`in`.tumcampusapp.api.navigatum.domain.NavigationEntity
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.Lecture
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Person

typealias PersonEntity = Person
typealias LectureEntity = Lecture

sealed class SearchResult {
    abstract val title: String
    abstract val subtitle: String

    data class Person(val person: PersonEntity) : SearchResult() {
        override val title: String
            get() = person.getFullName()

        override val subtitle: String
            get() = "Person"
    }

    data class Lecture(val lecture: LectureEntity) : SearchResult() {
        override val title: String
            get() = lecture.title

        override val subtitle: String
            get() = lecture.chairName ?: "Lecture"
    }

    data class NavigaRoom(val room: NavigationEntity) : SearchResult() {
        override val title: String
            get() = room.getFormattedName()

        override val subtitle: String
            get() = room.getFormattedSubtext()
    }

    data class Building(val building: NavigationEntity) : SearchResult() {
        override val title: String
            get() = building.getFormattedName()

        override val subtitle: String
            get() = building.getFormattedSubtext()
    }
}
