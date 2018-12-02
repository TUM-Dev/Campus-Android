package de.tum.`in`.tumcampusapp.component.ui.cafeteria.di

import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository
import de.tum.`in`.tumcampusapp.database.TcaDb

@Module
class CafeteriaModule {

    @Provides
    fun provideCafeteriaLocalRepository(
            database: TcaDb
    ): CafeteriaLocalRepository = CafeteriaLocalRepository(database)

    @Provides
    fun provideCafeteriaRemoteRepository(
            tumCabeClient: TUMCabeClient,
            localRepository: CafeteriaLocalRepository
    ): CafeteriaRemoteRepository = CafeteriaRemoteRepository(tumCabeClient, localRepository)

}
