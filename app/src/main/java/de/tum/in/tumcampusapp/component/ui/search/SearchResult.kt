package de.tum.`in`.tumcampusapp.component.ui.search

import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.Lecture
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Person
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model.RoomFinderRoom

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

    data class Room(val room: RoomFinderRoom) : SearchResult() {
        override val title: String
            get() = room.info

        override val subtitle: String
            get() = room.formattedAddress
    }

    data class Lecture(val lecture: LectureEntity) : SearchResult() {
        override val title: String
            get() = lecture.title

        override val subtitle: String
            get() = lecture.chairName ?: "Lecture"
    }
}