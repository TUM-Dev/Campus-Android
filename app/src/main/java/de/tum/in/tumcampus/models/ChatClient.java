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
        ChatRoom getChatRoom(@Path("groupId") String id);

        @POST("/rooms/{groupId}/join/")
        void joinChatRoom(@Path("groupId") String groupId, @Body ChatVerification verification, Callback<ChatRoom> cb);

        @POST("/rooms/{groupId}/leave/")
        void leaveChatRoom(@Path("groupId") String groupId, @Body ChatVerification verification, Callback<ChatRoom> cb);

        @PUT("/rooms/{groupId}/messages/")
        ChatMessage sendMessage(@Path("groupId") String groupId, @Body ChatMessage message);

        @POST("/rooms/{groupId}/messages/{page}/")
        ArrayList<ChatMessage> getMessages(@Path("groupId") String groupId, @Path("page") long page, @Body ChatVerification verification);

        @POST("/rooms/{groupId}/messages/")
        ArrayList<ChatMessage> getNewMessages(@Path("groupId") String groupId, @Body ChatVerification verification);

        @POST("/members/")
        ChatMember createMember(@Body ChatMember chatMember);

        @GET("/members/{lrz_id}/")
        ChatMember getMember(@Path("lrz_id") String lrzId);

        @POST("/members/{memberId}/pubkeys/")
        void uploadPublicKey(@Path("memberId") String memberId, @Body ChatPublicKey publicKey, Callback<ChatPublicKey> cb);

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

    public ChatRoom getChatRoom(String id) {
        return service.getChatRoom(id);
    }

    public ChatMember createMember(ChatMember chatMember) {
        return service.createMember(chatMember);
    }

    public ChatMember getMember(String lrzId) {
        return service.getMember(lrzId);
    }

    public void joinChatRoom(ChatRoom chatRoom, ChatVerification verification, Callback<ChatRoom> cb) {
        service.joinChatRoom(chatRoom.getGroupId(), verification, cb);
    }

    public void leaveChatRoom(ChatRoom chatRoom, ChatVerification verification, Callback<ChatRoom> cb) {
        service.leaveChatRoom(chatRoom.getGroupId(), verification, cb);
    }

    public ChatMessage sendMessage(String groupId, ChatMessage chatMessageCreate) {
        return service.sendMessage(groupId, chatMessageCreate);
    }

    public ArrayList<ChatMessage> getMessages(String groupId, long page, @Body ChatVerification verification) {
        return service.getMessages(groupId, page, verification);
    }

    public ArrayList<ChatMessage> getNewMessages(String groupId, @Body ChatVerification verification) {
        return service.getNewMessages(groupId, verification);
    }

    public void uploadPublicKey(String memberId, ChatPublicKey publicKey, Callback<ChatPublicKey> cb) {
        service.uploadPublicKey(memberId, publicKey, cb);
    }

    public List<ChatRoom> getMemberRooms(String memberId, ChatVerification verification) {
        return service.getMemberRooms(memberId, verification);
    }

    public void getPublicKeysForMember(ChatMember member, Callback<List<ChatPublicKey>> cb) {
        service.getPublicKeysForMember(member.getUserId(), cb);
    }

    public void uploadRegistrationId(String memberId, ChatRegistrationId regId, Callback<ChatRegistrationId> cb) {
        service.uploadRegistrationId(memberId, regId, cb);
    }
}