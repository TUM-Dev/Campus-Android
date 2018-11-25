package de.tum.`in`.tumcampusapp.component.ui.studyroom.di

import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.component.ui.studyroom.StudyRoomGroupLocalRepository
import de.tum.`in`.tumcampusapp.database.TcaDb

@Module
class StudyRoomsModule {

    @Provides
    fun provideStudyRoomGroupLocalRepository(
            database: TcaDb
    ): StudyRoomGroupLocalRepository {
        return StudyRoomGroupLocalRepository(database)
    }

}
