package de.tum.in.tumcampusapp.component.ui.chat;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeVerification;
import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineClient;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.Lecture;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.LecturesResponse;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMember;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoom;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoomAndLastMessage;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoomDbRow;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.ProvidesCard;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Response;

/**
 * TUMOnline cache manager, allows caching of TUMOnline requests
 */
public class ChatRoomController implements ProvidesCard {

    private Context mContext;
    private final ChatRoomDao chatRoomDao;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public ChatRoomController(Context context) {
        mContext = context;
        TcaDb db = TcaDb.getInstance(context);
        chatRoomDao = db.chatRoomDao();
    }

    /**
     * Gets all chat rooms that you have joined(1)/not joined(0) for the specified room.
     *
     * @param joined chat room 1=joined, 0=not joined/left chat room, -1=not joined
     * @return List of chat messages
     */
    public List<ChatRoomAndLastMessage> getAllByStatus(int joined) {
        if (joined == ChatRoom.MODE_JOINED) {
            return chatRoomDao.getAllRoomsJoinedList();
        } else {
            return chatRoomDao.getAllRoomsNotJoinedList();
        }
    }

    /**
     * Saves the given lectures into database
     */
    public void createLectureRooms(Iterable<Lecture> lectures) {
        // Create a Set of all existing lectures
        List<Integer> roomLvIds = chatRoomDao.getLvIds();
        Collection<String> set = new HashSet<>();
        for (Integer id : roomLvIds) {
            set.add(String.valueOf(id));
        }

        // Add lectures that are not yet in DB
        for (Lecture lecture : lectures) {
            if (!set.contains(lecture.getLectureId())) {
                chatRoomDao.replaceRoom(ChatRoomDbRow.Companion.fromLecture(lecture));
            }
        }
    }

    /**
     * Saves the given chat rooms into database
     */
    public void replaceIntoRooms(Collection<ChatRoom> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            Utils.log("No rooms passed, can't insert anything.");
            return;
        }

        chatRoomDao.markAsNotJoined();
        Utils.log("reset join status of all rooms");

        /* TODO(jacqueline8711): load the last messages when joining the chat (here or somewhere else?)
        TUMCabeVerification verification;
        try {
            ChatMember currentChatMember = Utils.getSetting(context, Const.CHAT_MEMBER, ChatMember.class);
            if (currentChatMember != null) {
                verification = TUMCabeVerification.create(context, currentChatMember);
            }
        } catch (NoPrivateKey noPrivateKey) {
            return; //In this case we simply cannot do anything
        }

        ChatMessageRemoteRepository remoteRepository = ChatMessageRemoteRepository.INSTANCE;
        remoteRepository.setTumCabeClient(TUMCabeClient.getInstance(context));
        ChatMessageLocalRepository localRepository = ChatMessageLocalRepository.INSTANCE;
        localRepository.setDb(TcaDb.getInstance(context));
        ChatMessageViewModel chatMessageViewModel = new ChatMessageViewModel(localRepository, remoteRepository, new CompositeDisposable());
        */

        for (ChatRoom room : rooms) {
            String roomName = room.getActualName();
            String semester = room.getSemester();

            List<Integer> roomIds = chatRoomDao.getGivenLecture(roomName, semester);
            if (roomIds.isEmpty()) {
                ChatRoomDbRow chatRoom = new ChatRoomDbRow(room.getId(), roomName, "", semester, 1, 0,
                                                           "", room.getMembers(), -1);
                chatRoomDao.replaceRoom(chatRoom);
            } else {
                //in dao
                chatRoomDao.updateRoomToJoined(room.getId(), room.getMembers(), roomName, semester);
                /* TODO(jacqueline8711) load the last messages when joining the chat
                chatMessageViewModel.getNewMessages(room.getId(), verification,
                                                    context instanceof ChatMessageViewModel.DataLoadInterface ?
                                                    (ChatMessageViewModel.DataLoadInterface)context : null);
                Utils.log("Loading some messages for a newly joined chatroom");*/
            }
        }

    }

    public void join(ChatRoom currentChatRoom) {
        chatRoomDao.updateJoinedRooms(currentChatRoom.getId(), currentChatRoom.getActualName(), currentChatRoom.getSemester());
    }

    public void leave(ChatRoom currentChatRoom) {
        chatRoomDao.updateLeftRooms(currentChatRoom.getId(), currentChatRoom.getActualName(), currentChatRoom.getSemester());
    }

    @NotNull
    @Override
    public List<Card> getCards(CacheControl cacheControl) {
        List<Card> results = new ArrayList<>();

        try {
            Response<LecturesResponse> response = TUMOnlineClient
                    .getInstance(mContext)
                    .getPersonalLectures(cacheControl)
                    .execute();

            if (response != null) {
                LecturesResponse lecturesResponse = response.body();

                if (lecturesResponse != null) {
                    List<Lecture> lectures = lecturesResponse.getLectures();
                    createLectureRooms(lectures);
                }
            }

            // Join all new chat rooms
            if (Utils.getSettingBool(mContext, Const.AUTO_JOIN_NEW_ROOMS, false)) {
                TUMCabeClient client = TUMCabeClient.getInstance(mContext);
                List<String> newRooms = this.getNewUnjoined();
                ChatMember currentChatMember = Utils.getSetting(
                        mContext, Const.CHAT_MEMBER, ChatMember.class);

                for (String roomId : newRooms) {
                    // Join chat room
                    try {
                        ChatRoom currentChatRoom = new ChatRoom(roomId);
                        TUMCabeVerification verification = TUMCabeVerification.create(mContext, null);
                        if (verification == null) {
                            return results;
                        }

                        currentChatRoom = client.createRoom(currentChatRoom, verification);
                        if (currentChatRoom != null) {
                            this.join(currentChatRoom);
                        }
                    } catch (IOException e) {
                        Utils.log(e, " - error occured while creating the room!");
                    }
                }
            }

            // Get all rooms that have unread messages
            List<ChatRoomDbRow> rooms = chatRoomDao.getUnreadRooms();
            if (!rooms.isEmpty()) {
                for (ChatRoomDbRow room : rooms) {
                    ChatMessagesCard card = new ChatMessagesCard(mContext, room);
                    results.add(card.getIfShowOnStart());
                }
            }

            return results;
        } catch (IOException e) {
            Utils.log(e);
            return results;
        }
    }

    private List<String> getNewUnjoined() {
        List<ChatRoomDbRow> rooms = chatRoomDao.getNewUnjoined();
        if (rooms.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<>(rooms.size());
        for (ChatRoomDbRow room : rooms) {
            list.add(String.valueOf(room.getSemesterId() + ':' + room.getName()));
        }
        return list;
    }
}