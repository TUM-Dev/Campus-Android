package de.tum.in.tumcampus.models;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampus.auxiliary.Const;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public class ChatClient {

	private static final String API_URL = Const.CHAT_URL;
	
	private static ChatClient instance = null;
	private ChatService service = null;
	
	private ChatClient() {
		RestAdapter restAdapter = new RestAdapter.Builder()
				.setEndpoint(API_URL)
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.build();
		service = restAdapter.create(ChatService.class);
	}
	
	public static ChatClient getInstance() {
		if (instance == null) {
			instance = new ChatClient();
		}
		return instance;
	}
	
	private interface ChatService {
		
		@POST("/chat_rooms/")
		void createChatRoom(@Body ChatRoom chatRoom, Callback<ChatRoom> cb);
		
		@GET("/chat_rooms/")
		List<ChatRoom> getChatRoomWithName(@Query("name") String name);
		
		@GET("/chat_rooms/{groupId}")
		ChatRoom getChatRoom(@Path("groupId") String id);
		
		@POST("/members/")
		ChatMember createMember(@Body ChatMember chatMember);
		
		@GET("/members/")
		List<ChatMember> getMember(@Query("lrz_id") String lrzId);
		
		@POST("/chat_rooms/{groupId}/add_member/")
		void joinChatRoom(@Path("groupId") String groupId, @Body ChatMember chatMember, Callback<ChatRoom> cb);
		
		@POST("/chat_rooms/{groupId}/remove_member/")
		void leaveChatRoom(@Path("groupId") String groupId, @Body ChatMember chatMember, Callback<ChatRoom> cb);
		
		@POST("/chat_rooms/{groupId}/messages/")
		CreateChatMessage sendMessage(@Path("groupId") String groupId, @Body CreateChatMessage chatMessage);
		
		@GET("/chat_rooms/{groupId}/messages/")
		void getMessages(@Path("groupId") String groupId, @Query("page") int page, Callback<ArrayList<ListChatMessage>> cb);
		
		@POST("/members/{memberId}/pubkeys/")
		void uploadPublicKey(@Path("memberId") String memberId, @Body ChatPublicKey publicKey, Callback<ChatPublicKey> cb);
		
		@GET("/members/{memberId}/pubkeys/")
		List<ChatPublicKey> getPublicKeysForMember(@Path("memberId") String memberId);
		
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
	
	public List<ChatMember> getMember(String lrzId) {
		return service.getMember(lrzId);
	}
	
	public void joinChatRoom(ChatRoom chatRoom, ChatMember chatMember, Callback<ChatRoom> cb) {
		service.joinChatRoom(chatRoom.getGroupId(), chatMember, cb);
	}
	
	public void leaveChatRoom(ChatRoom chatRoom, ChatMember chatMember, Callback<ChatRoom> cb) {
		service.leaveChatRoom(chatRoom.getGroupId(), chatMember, cb);
	}
	
	public CreateChatMessage sendMessage(String groupId, CreateChatMessage chatMessage) {
		return service.sendMessage(groupId, chatMessage);
	}
	
	public void getMessages(String groupId, int page, Callback<ArrayList<ListChatMessage>> cb) {
		service.getMessages(groupId, page, cb);
	}
	
	public void uploadPublicKey(String memberId, ChatPublicKey publicKey, Callback<ChatPublicKey> cb) {
		service.uploadPublicKey(memberId, publicKey, cb);
	}
	
	public List<ChatPublicKey> getPublicKeysForMember(String memberId) {
		return service.getPublicKeysForMember(memberId);
	}
	
	public void uploadRegistrationId(String memberId, ChatRegistrationId regId, Callback<ChatRegistrationId> cb) {
		service.uploadRegistrationId(memberId, regId, cb);
	}
}