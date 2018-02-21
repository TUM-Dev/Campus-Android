package de.tum.in.tumcampusapp.managers;

import android.content.Context;

import com.google.common.base.Optional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.cards.ChatMessagesCard;
import de.tum.in.tumcampusapp.cards.generic.Card;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dao.ChatRoomDao;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
import de.tum.in.tumcampusapp.models.dbEntities.ChatRoomAndLastMessage;
import de.tum.in.tumcampusapp.models.dbEntities.ChatRoomDbRow;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatRoom;
import de.tum.in.tumcampusapp.models.tumcabe.ChatVerification;
import de.tum.in.tumcampusapp.models.tumo.LecturesSearchRow;
import de.tum.in.tumcampusapp.models.tumo.LecturesSearchRowSet;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;

/**
 * TUMOnline cache manager, allows caching of TUMOnline requests
 */
public class ChatRoomManager implements Card.ProvidesCard {

    private final ChatRoomDao chatRoomDao;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public ChatRoomManager(Context context) {
        chatRoomDao = TcaDb.getInstance(context)
                           .chatRoomDao();
    }

    /**
     * Gets all chat rooms that you have joined(1)/not joined(0) for the specified room
     *
     * @param joined chat room 1=joined, 0=not joined/left chat room, -1=not joined
     * @return List of chat messages
     */
    public List<ChatRoomAndLastMessage> getAllByStatus(int joined) {
        if (joined == 1) {
            return chatRoomDao.getAllRoomsJoinedList();
        } else {
            return chatRoomDao.getAllRoomsNotJoinedList();
        }
    }

    /**
     * Saves the given lecture into database
     */
    public void replaceInto(LecturesSearchRow lecture) {
        List<Integer> givenLecture = chatRoomDao.getGivenLecture(lecture.getTitel(), lecture.getSemester_id());
        if (givenLecture.size() >= 1) {
            chatRoomDao.updateRoom(lecture.getSemester_name(), Integer.valueOf(lecture.getStp_lv_nr()),
                                   lecture.getVortragende_mitwirkende(), lecture.getTitel(), lecture.getSemester_id());
        } else {
            ChatRoomDbRow room = new ChatRoomDbRow(-1, lecture.getTitel(), lecture.getSemester_name(), lecture.getSemester_id(), -1, Integer.parseInt(lecture.getStp_lv_nr()), lecture.getVortragende_mitwirkende(), 0);
            chatRoomDao.replaceRoom(room);
        }
    }

    /**
     * Saves the given lectures into database
     */
    public void replaceInto(List<LecturesSearchRow> lectures) {
        Collection<String> set;
        List<Integer> roomIds = chatRoomDao.getIds();

        set = new HashSet<>();
        if (roomIds.size() >= 1) {
            for (Integer id : roomIds) {
                set.add(String.valueOf(id));
            }
        }

        for (LecturesSearchRow lecture : lectures) {
            if (!set.contains(lecture.getStp_lv_nr())) {
                replaceInto(lecture);
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

        for (ChatRoom room : rooms) {
            String roomName = room.getActualName();
            String semester = room.getSemester();

            List<Integer> roomIds = chatRoomDao.getGivenLecture(roomName, semester);
            if (roomIds.size() >= 1) {
                //in dao
                chatRoomDao.updateRoomToJoined(room.getId(), room.getMembers(), roomName, semester);
            } else {
                ChatRoomDbRow chatRoom = new ChatRoomDbRow(room.getId(), roomName, "", semester, 1, 0, "", room.getMembers());
                chatRoomDao.replaceRoom(chatRoom);
            }
        }

    }

    public void join(ChatRoom currentChatRoom) {
        chatRoomDao.updateJoinedRooms(currentChatRoom.getId(), currentChatRoom.getName()
                                                                              .substring(4), currentChatRoom.getName()
                                                                                                            .substring(0, 3));
    }

    public void leave(ChatRoom currentChatRoom) {
        chatRoomDao.updateLeftRooms(currentChatRoom.getId(), currentChatRoom.getName()
                                                                            .substring(4), currentChatRoom.getName()
                                                                                                          .substring(0, 3));
    }

    @Override
    public void onRequestCard(Context context) {
        // Get all of the users lectures and save them as possible chat rooms
        TUMOnlineRequest<LecturesSearchRowSet> requestHandler = new TUMOnlineRequest<>(TUMOnlineConst.Companion.getLECTURES_PERSONAL(), context, true);
        Optional<LecturesSearchRowSet> lecturesList = requestHandler.fetch();
        if (lecturesList.isPresent() && lecturesList.get()
                                                    .getLehrveranstaltungen() != null) {
            List<LecturesSearchRow> lectures = lecturesList.get()
                                                           .getLehrveranstaltungen();
            this.replaceInto(lectures);
        }

        // Join all new chat rooms
        if (Utils.getSettingBool(context, Const.AUTO_JOIN_NEW_ROOMS, false)) {
            List<String> newRooms = this.getNewUnjoined();
            ChatMember currentChatMember = Utils.getSetting(context, Const.CHAT_MEMBER, ChatMember.class);
            for (String roomId : newRooms) {
                // Join chat room
                try {
                    ChatRoom currentChatRoom = new ChatRoom(roomId);
                    currentChatRoom = TUMCabeClient.getInstance(context)
                                                   .createRoom(currentChatRoom, ChatVerification.Companion.getChatVerification(context, currentChatMember));
                    if (currentChatRoom != null) {
                        this.join(currentChatRoom);
                    }
                } catch (IOException e) {
                    Utils.log(e, " - error occured while creating the room!");
                } catch (NoPrivateKey noPrivateKey) {
                    return;
                }
            }
        }

        // Get all rooms that have unread messages
        List<ChatRoomDbRow> rooms = chatRoomDao.getUnreadRooms();
        if (!rooms.isEmpty()) {
            for (ChatRoomDbRow room : rooms) {
                ChatMessagesCard card = new ChatMessagesCard(context, room);
                card.apply();
            }
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