package de.tum.`in`.tumcampusapp.component.ui.studyroom.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.studyroom.StudyRoomGroupDetailsFragment
import de.tum.`in`.tumcampusapp.component.ui.studyroom.StudyRoomsActivity

@Subcomponent(modules = [StudyRoomsModule::class])
interface StudyRoomsComponent {

    fun inject(studyRoomsActivity: StudyRoomsActivity)

    fun inject(studyRoomsFragment: StudyRoomGroupDetailsFragment)

    @Subcomponent.Builder
    interface Builder {

        fun studyRoomsModule(studyRoomsModule: StudyRoomsModule): Builder

        fun build(): StudyRoomsComponent

    }

}
