package de.tum.`in`.tumcampusapp.component.ui.chat

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.model.TUMCabeVerification
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatRoom
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardsProvider
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import java.io.IOException
import java.util.*
import javax.inject.Inject

class ChatRoomCardsProvider @Inject constructor(
        private val context: Context,
        private val tumOnlineClient: TUMOnlineClient,
        private val tumCabeClient: TUMCabeClient,
        private val chatRoomController: ChatRoomController,
        private val database: TcaDb
) : CardsProvider {

    override fun provideCards(cacheControl: CacheControl): List<Card> {
        val results = ArrayList<Card>()

        try {
            val response = tumOnlineClient
                    .getPersonalLectures(cacheControl)
                    .execute()

            if (response != null) {
                val lecturesResponse = response.body()

                if (lecturesResponse != null) {
                    val lectures = lecturesResponse.lectures
                    chatRoomController.createLectureRooms(lectures)
                }
            }

            // Join all new chat rooms
            if (Utils.getSettingBool(context, Const.AUTO_JOIN_NEW_ROOMS, false)) {
                val newRooms = chatRoomController.newUnjoined

                for (roomId in newRooms) {
                    // Join chat room
                    try {
                        var currentChatRoom: ChatRoom? = ChatRoom(roomId)
                        val verification = TUMCabeVerification.create(context, null) ?: return results

                        currentChatRoom = tumCabeClient.createRoom(currentChatRoom, verification)
                        if (currentChatRoom != null) {
                            chatRoomController.join(currentChatRoom)
                        }
                    } catch (e: IOException) {
                        Utils.log(e, " - error occured while creating the room!")
                    }
                }
            }

            // Get all rooms that have unread messages
            val rooms = database.chatRoomDao().unreadRooms
            if (!rooms.isEmpty()) {
                for (room in rooms) {
                    val card = ChatMessagesCard(context, room)
                    card.getIfShowOnStart()?.let {
                        results.add(it)
                    }
                }
            }

            return results
        } catch (e: IOException) {
            Utils.log(e)
            return results
        }

    }

}
