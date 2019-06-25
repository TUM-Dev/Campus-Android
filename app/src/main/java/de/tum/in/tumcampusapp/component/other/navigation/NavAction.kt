package de.tum.`in`.tumcampusapp.component.other.navigation

import android.os.Parcelable
import androidx.fragment.app.Fragment
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarFragment
import de.tum.`in`.tumcampusapp.component.tumui.grades.GradesFragment
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderFragment
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeesFragment
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.fragment.CafeteriaFragment
import de.tum.`in`.tumcampusapp.component.ui.chat.ChatRoomsFragment
import de.tum.`in`.tumcampusapp.component.ui.news.NewsFragment
import de.tum.`in`.tumcampusapp.component.ui.studyroom.StudyRoomsFragment
import de.tum.`in`.tumcampusapp.component.ui.ticket.fragment.EventsFragment
import kotlinx.android.parcel.Parcelize

sealed class NavAction : Parcelable {

    abstract fun createDestination(): Fragment

    @Parcelize
    data class Cafeteria(val id: Int) : NavAction(), Parcelable {
        override fun createDestination(): Fragment = CafeteriaFragment.newInstance(id)
    }

    @Parcelize
    data class RoomFinder(val roomNumber: String) : NavAction(), Parcelable {
        override fun createDestination(): Fragment = RoomFinderFragment.newInstance()
    }

    @Parcelize
    object Chat : NavAction(), Parcelable {
        override fun createDestination(): Fragment = ChatRoomsFragment.newInstance()
    }

    @Parcelize
    object News : NavAction(), Parcelable {
        override fun createDestination(): Fragment = NewsFragment.newInstance()
    }

    @Parcelize
    object Events : NavAction(), Parcelable {
        override fun createDestination(): Fragment = EventsFragment.newInstance()
    }

    @Parcelize
    object Tuition : NavAction(), Parcelable {
        override fun createDestination(): Fragment = TuitionFeesFragment.newInstance()
    }

    @Parcelize
    object Grades : NavAction(), Parcelable {
        override fun createDestination(): Fragment = GradesFragment.newInstance()
    }

    @Parcelize
    object Calendar : NavAction(), Parcelable {
        override fun createDestination(): Fragment = CalendarFragment.newInstance()
    }

    @Parcelize
    object Mensa : NavAction(), Parcelable {
        override fun createDestination(): Fragment = CafeteriaFragment.newInstance()
    }

    @Parcelize
    object StudyRooms : NavAction(), Parcelable {
        override fun createDestination(): Fragment = StudyRoomsFragment.newInstance()
    }

}
