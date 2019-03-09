package de.tum.`in`.tumcampusapp.component.ui.cafeteria.di

import android.content.Context
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.service.DownloadWorker

/**
 * This module provides dependencies needed in the cafeteria functionality, for instance the
 * [CafeteriaManager] and the data repositories.
 */
@Module
class CafeteriaModule {

    @Provides
    fun provideCafeteriaManager(
            context: Context
    ): CafeteriaManager = CafeteriaManager(context)

    @Provides
    fun provideCafeteriaLocalRepository(
            database: TcaDb
    ): CafeteriaLocalRepository = CafeteriaLocalRepository(database)

    @Provides
    fun provideCafeteriaRemoteRepository(
            tumCabeClient: TUMCabeClient,
            localRepository: CafeteriaLocalRepository
    ): CafeteriaRemoteRepository = CafeteriaRemoteRepository(tumCabeClient, localRepository)

    @Provides
    fun provideCafeteriaDownloadAction(
            menuManager: CafeteriaMenuManager,
            remoteRepository: CafeteriaRemoteRepository
    ): DownloadWorker.Action = CafeteriaDownloadAction(menuManager, remoteRepository)

}
