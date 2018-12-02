package de.tum.`in`.tumcampusapp.di

import android.content.Context
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.database.TcaDb
import javax.inject.Singleton

@Module
class AppModule(private val context: Context) {

    @Singleton
    @Provides
    fun provideContext(): Context = context

    @Singleton
    @Provides
    fun provideTUMCabeClient(
            context: Context
    ): TUMCabeClient = TUMCabeClient.getInstance(context)

    @Singleton
    @Provides
    fun provideTUMOnlineClient(
            context: Context
    ): TUMOnlineClient = TUMOnlineClient.getInstance(context)

    @Singleton
    @Provides
    fun provideDatabase(
            context: Context
    ): TcaDb = TcaDb.getInstance(context)

}
