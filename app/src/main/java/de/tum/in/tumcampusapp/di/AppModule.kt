package de.tum.`in`.tumcampusapp.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.ui.news.RealTopNewsStore
import de.tum.`in`.tumcampusapp.component.ui.news.TopNewsStore
import de.tum.`in`.tumcampusapp.database.TcaDb
import javax.inject.Singleton

/**
 * This module provides dependencies that are needed throughout the entire app, for instance the
 * database or the Retrofit clients.
 */
@Module
abstract class AppModule {

    @Singleton
    @Binds
    abstract fun bindTopNewsStore(impl: RealTopNewsStore): TopNewsStore

    @Module
    companion object {

        @JvmStatic
        @Singleton
        @Provides
        fun provideSharedPreferences(
            context: Context
        ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        @JvmStatic
        @Singleton
        @Provides
        fun provideTUMCabeClient(
            context: Context
        ): TUMCabeClient = TUMCabeClient.getInstance(context)

        @JvmStatic
        @Singleton
        @Provides
        fun provideTUMOnlineClient(
            context: Context
        ): TUMOnlineClient = TUMOnlineClient.getInstance(context)

        @JvmStatic
        @Singleton
        @Provides
        fun provideDatabase(
            context: Context
        ): TcaDb = TcaDb.getInstance(context)

        @JvmStatic
        @Singleton
        @Provides
        fun provideLocalBroadcastManager(
            context: Context
        ): LocalBroadcastManager = LocalBroadcastManager.getInstance(context)
    }
}
