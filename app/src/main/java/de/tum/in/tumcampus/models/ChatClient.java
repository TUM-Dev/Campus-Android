package de.tum.in.tumcampus.models;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

public class ChatClient {

    private static final String API_URL = Const.CHAT_URL;

    private static ChatClient instance = null;
    private ChatService service = null;

    private static Context c = null;


    private ChatClient() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(new AndroidLog("suqmadiq"))
                .setRequestInterceptor(requestInterceptor)
                .build();
        service = restAdapter.create(ChatService.class);
    }

    public static ChatClient getInstance(Context c) {
        ChatClient.c = c;
        if (instance == null) {
            instance = new ChatClient();
        }
        return instance;
    }

    RequestInterceptor requestInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            request.addHeader("X-DEVICE-ID", NetUtils.getDeviceID(ChatClient.c));
        }
    };

    private interface ChatService {

        @POST("/rooms/")
        void createChatRoom(@Body ChatRoom chatRoom, Callback<ChatRoom> cb);

        @GET("/rooms/{roomName}/")
        List<ChatRoom> getChatRoomWithName(@Path("roomName") String roomName);

        @GET("/rooms/{groupId}")
        ChatRoom getChatRoom(@Path("groupId") int id);

        @POST("/rooms/{groupId}/join/")
        void joinChatRoom(@Path("groupId") int roomId, @Body ChatVerification verification, Callback<ChatRoom> cb);

        @POST("/rooms/{groupId}/leave/")
        void leaveChatRoom(@Path("groupId") int roomId, @Body ChatVerification verification, Callback<ChatRoom> cb);

        @PUT("/rooms/{groupId}/messages/")
        ChatMessage sendMessage(@Path("groupId") int roomId, @Body ChatMessage message);

        @POST("/rooms/{groupId}/messages/{page}/")
        ArrayList<ChatMessage> getMessages(@Path("groupId") int roomId, @Path("page") long page, @Body ChatVerification verification);

        @POST("/rooms/{groupId}/messages/")
        ArrayList<ChatMessage> getNewMessages(@Path("groupId") int roomId, @Body ChatVerification verification);

        @POST("/members/")
        ChatMember createMember(@Body ChatMember chatMember);

        @GET("/members/{lrz_id}/")
        ChatMember getMember(@Path("lrz_id") String lrzId);

        @POST("/members/{memberId}/pubkeys/")
        ChatPublicKey uploadPublicKey(@Path("memberId") String memberId, @Body ChatPublicKey publicKey);

        @POST("/members/{memberId}/rooms/")
        List<ChatRoom> getMemberRooms(@Path("memberId") String memberId, @Body ChatVerification verification);

        @GET("/members/{memberId}/pubkeys/")
        void getPublicKeysForMember(@Path("memberId") String memberId, Callback<List<ChatPublicKey>> cb);

        @POST("/members/{memberId}/registration_ids/add_id")
        void uploadRegistrationId(@Path("memberId") String memberId, @Body ChatRegistrationId regId, Callback<ChatRegistrationId> cb);

    }

    public void createGroup(ChatRoom chatRoom, Callback<ChatRoom> cb) {
        service.createChatRoom(chatRoom, cb);
    }

    public List<ChatRoom> getChatRoomWithName(ChatRoom chatRoom) {
        return service.getChatRoomWithName(chatRoom.getName());
    }

    public ChatRoom getChatRoom(int id) {
        return service.getChatRoom(id);
    }

    public ChatMember createMember(ChatMember chatMember) {
        return service.createMember(chatMember);
    }

    public ChatMember getMember(String lrzId) {
        return service.getMember(lrzId);
    }

    public void joinChatRoom(ChatRoom chatRoom, ChatVerification verification, Callback<ChatRoom> cb) {
        service.joinChatRoom(chatRoom.getId(), verification, cb);
    }

    public void leaveChatRoom(ChatRoom chatRoom, ChatVerification verification, Callback<ChatRoom> cb) {
        service.leaveChatRoom(chatRoom.getId(), verification, cb);
    }

    public ChatMessage sendMessage(int roomId, ChatMessage chatMessageCreate) {
        return service.sendMessage(roomId, chatMessageCreate);
    }

    public ArrayList<ChatMessage> getMessages(int roomId, long page, @Body ChatVerification verification) {
        return service.getMessages(roomId, page, verification);
    }

    public ArrayList<ChatMessage> getNewMessages(int roomId, @Body ChatVerification verification) {
        return service.getNewMessages(roomId, verification);
    }

    public ChatPublicKey uploadPublicKey(String memberId, ChatPublicKey publicKey) {
        return service.uploadPublicKey(memberId, publicKey);
    }

    public List<ChatRoom> getMemberRooms(String memberId, ChatVerification verification) {
        return service.getMemberRooms(memberId, verification);
    }

    public void getPublicKeysForMember(ChatMember member, Callback<List<ChatPublicKey>> cb) {
        service.getPublicKeysForMember(member.getId(), cb);
    }

    public void uploadRegistrationId(String memberId, ChatRegistrationId regId, Callback<ChatRegistrationId> cb) {
        service.uploadRegistrationId(memberId, regId, cb);
    }
}