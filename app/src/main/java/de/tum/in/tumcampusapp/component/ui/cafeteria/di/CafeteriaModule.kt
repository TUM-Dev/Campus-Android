package de.tum.`in`.tumcampusapp.component.ui.cafeteria.di

import android.content.Context
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.cafeteria.CafeteriaAPIClient
import de.tum.`in`.tumcampusapp.component.other.locations.TumLocationManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaNotificationProvider
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaNotificationSettings
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaCardsProvider
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.interactors.FetchBestMatchMensaInteractor
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaMenuLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository
import de.tum.`in`.tumcampusapp.database.TcaDb
import org.jetbrains.anko.defaultSharedPreferences

@Module
class CafeteriaModule(private val context: Context) {

    @Provides
    fun provideCafeteriaManager(
            tumLocationManager: TumLocationManager,
            localRepository: CafeteriaLocalRepository,
            remoteRepository: CafeteriaRemoteRepository
    ): CafeteriaManager {
        return CafeteriaManager(context, tumLocationManager, localRepository, remoteRepository)
    }

    @Provides
    fun provideCafeteriaMenuManager(
            database: TcaDb,
            settings: CafeteriaNotificationSettings
    ): CafeteriaMenuManager {
        return CafeteriaMenuManager(context, database, settings)
    }

    @Provides
    fun provideCafeteriaMenuLocalRepository(
            database: TcaDb
    ): CafeteriaMenuLocalRepository {
        return CafeteriaMenuLocalRepository(database)
    }

    @Provides
    fun provideCafeteriaMenuRemoteRepository(
            cafeteriaMenuManager: CafeteriaMenuManager,
            localRepository: CafeteriaMenuLocalRepository,
            apiClient: CafeteriaAPIClient
    ): CafeteriaMenuRemoteRepository {
        return CafeteriaMenuRemoteRepository(cafeteriaMenuManager, localRepository, apiClient)
    }

    @Provides
    fun provideCafeteriaCardsProvider(
            cafeteriaManager: CafeteriaManager,
            localRepository: CafeteriaLocalRepository,
            tumLocationManager: TumLocationManager
    ) : CafeteriaCardsProvider {
        return CafeteriaCardsProvider(context, cafeteriaManager, localRepository, tumLocationManager)
    }

    @Provides
    fun provideFetchBestMatchMensaInteractor(
            cafeteriaManager: CafeteriaManager
    ): FetchBestMatchMensaInteractor {
        return FetchBestMatchMensaInteractor(cafeteriaManager)
    }

    @Provides
    fun provideCafeteriaNotificationProvider(
            tumLocationManager: TumLocationManager,
            cafeteriaManager: CafeteriaManager,
            cafeteriaMenuManager: CafeteriaMenuManager,
            localRepository: CafeteriaLocalRepository
    ): CafeteriaNotificationProvider {
        return CafeteriaNotificationProvider(
                context, tumLocationManager, cafeteriaManager, cafeteriaMenuManager, localRepository
        )
    }

    @Provides
    fun provideCafeteriaNotificationSettings(
            store: CafeteriaNotificationSettings.Store
    ): CafeteriaNotificationSettings {
        return CafeteriaNotificationSettings(store)
    }

    @Provides
    fun provideCafeteriaNotificationSettingsStore(): CafeteriaNotificationSettings.Store {
        return CafeteriaNotificationSettings.Store(context.defaultSharedPreferences)
    }

}
