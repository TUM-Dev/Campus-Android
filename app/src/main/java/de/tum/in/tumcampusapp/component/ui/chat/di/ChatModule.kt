package de.tum.`in`.tumcampusapp.component.ui.chat.di

import android.content.Context
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.ui.chat.ChatRoomCardsProvider
import de.tum.`in`.tumcampusapp.component.ui.chat.ChatRoomController
import de.tum.`in`.tumcampusapp.component.ui.chat.repository.ChatMessageLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.chat.repository.ChatMessageRemoteRepository
import de.tum.`in`.tumcampusapp.database.TcaDb

@Module
class ChatModule(private val context: Context) {

    @Provides
    fun provideChatRoomController(database: TcaDb): ChatRoomController {
        return ChatRoomController(database)
    }

    @Provides
    fun provideChatMessageLocalRepository(
            database: TcaDb
    ): ChatMessageLocalRepository {
        return ChatMessageLocalRepository(database)
    }

    @Provides
    fun provideChatMessageRemoteRepository(
            localRepository: ChatMessageLocalRepository,
            tumCabeClient: TUMCabeClient
    ): ChatMessageRemoteRepository {
        return ChatMessageRemoteRepository(context, localRepository, tumCabeClient)
    }

    @Provides
    fun provideChatRoomCardsProvider(
            tumOnlineClient: TUMOnlineClient,
            tumCabeClient: TUMCabeClient,
            chatRoomController: ChatRoomController,
            database: TcaDb
    ): ChatRoomCardsProvider {
        return ChatRoomCardsProvider(context, tumOnlineClient, tumCabeClient, chatRoomController, database)
    }

}
