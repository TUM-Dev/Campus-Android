package de.tum.in.tumcampusapp.managers;

import android.content.Context;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Date;

import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dao.ChatMessageDao;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;
import de.tum.in.tumcampusapp.models.tumcabe.ChatVerification;
import de.tum.in.tumcampusapp.repository.ChatMessageLocalRepository;
import de.tum.in.tumcampusapp.repository.ChatMessageRemoteRepository;
import de.tum.in.tumcampusapp.viewmodel.ChatMessageViewModel;
import io.reactivex.disposables.CompositeDisposable;

/**
 * TUMOnline cache manager, allows caching of TUMOnline requests
 */
public class ChatMessageManager {

    private final int mChatRoom;
    private Context mContext;
    private final ChatMessageDao chatMessageDao;
    private final ChatMessageViewModel chatMessageViewModel;
    private final CompositeDisposable compositeDisposable;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public ChatMessageManager(Context context, int room) {
        mContext = context;
        mChatRoom = room;
        TcaDb tcaDb = TcaDb.getInstance(context);
        chatMessageDao = tcaDb.chatMessageDao();
        chatMessageDao.deleteOldEntries();
        compositeDisposable = new CompositeDisposable();
        ChatMessageLocalRepository localRepository = ChatMessageLocalRepository.INSTANCE;
        localRepository.setDb(tcaDb);
        ChatMessageRemoteRepository remoteRepository = ChatMessageRemoteRepository.INSTANCE;
        remoteRepository.setTumCabeClient(TUMCabeClient.getInstance(context));
        chatMessageViewModel = new ChatMessageViewModel(localRepository, remoteRepository, compositeDisposable);
    }

    /**
     * Saves the given message into database
     */
    public void replaceInto(ChatMessage m, int memberId) {
        if (m == null || m.getText() == null) {
            Utils.log("Message empty");
            return;
        }

        Utils.logv("replace " + m.getText() + " " + m.getId() + " " + m.getPrevious() + " " + m.getSendingStatus());

        // Query read status from the previous message and use this read status as well if it is "0"
        boolean read = memberId == m.getMember()
                                    .getId();
        int readStatus = chatMessageDao.getRead(m.getId());
        if (readStatus == 1) {
            read = true;
        }
        m.setSendingStatus(ChatMessage.STATUS_SENT);
        m.setRead(read);
        replaceMessage(m);

    }

    public void replaceMessage(ChatMessage m) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        Date date;
        try {
            date = formatter.parse(m.getTimestamp());
        } catch (ParseException e) {
            date = new Date();
        }
        m.setTimestamp(Utils.getDateTimeString(date));
        chatMessageDao.replaceMessage(m);
    }

    /**
     * Saves the given message into database
     */
    public void replaceInto(List<ChatMessage> m) {
        ChatMember member = Utils.getSetting(mContext, Const.CHAT_MEMBER, ChatMember.class);

        if (member == null) {
            return;
        }

        for (ChatMessage msg : m) {
            replaceInto(msg, member.getId());
        }
    }

    public List<ChatMessage> getNewMessages(ChatMember member, int messageId) throws NoPrivateKey, IOException {
        if (messageId == -1) {
            chatMessageViewModel.getNewMessages(mChatRoom, ChatVerification.Companion.getChatVerification(mContext, member));
        } else {
            chatMessageViewModel.getMessages(mChatRoom, messageId, ChatVerification.Companion.getChatVerification(mContext, member));
        }
        return chatMessageDao.getUnreadList(mChatRoom);
    }
}
