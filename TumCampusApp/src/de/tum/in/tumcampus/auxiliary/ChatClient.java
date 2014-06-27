package de.tum.in.tumcampus.auxiliary;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatMessage;
import de.tum.in.tumcampus.models.ChatRoom;

public class ChatClient {

	public static final String API_URL = "http://ec2-54-74-61-201.eu-west-1.compute.amazonaws.com";
	
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
		List<ChatRoom> getChatRoom(@Query("name") String name);
		
		@POST("/members/")
		ChatMember createMember(@Body ChatMember chatMember);
		
		@GET("/members/")
		List<ChatMember> getMember(@Query("lrz_id") String lrzId);
		
		@POST("/chat_rooms/{groupId}/add_member/")
		void joinChatRoom(@Path("groupId") String groupId, @Body ChatMember chatMember, Callback<ChatRoom> cb);
		
		@DELETE("/chat_rooms/{groupId}/members/{lrzId}/")
		void leaveChatRoom(@Path("groupId") String groupdId, @Path("lrzId") String lrzId);
		
		@POST("/chat_rooms/{groupId}/messages/")
		void sendMessage(@Path("groupId") String groupId, @Body ChatMessage chatMessage, Callback<ChatMessage> cb);
		
		@GET("/chat_rooms/{groupId}/messages/")
		List<ChatMessage> getMessages(@Path("groupId") String groupId);
		
		@GET("/chat_rooms/{groupId}/messages/")
		void getMessagesCb(@Path("groupId") String groupId, Callback<List<ChatMessage>> cb);
	}
	
	public void createGroup(ChatRoom chatRoom, Callback<ChatRoom> cb) {
		service.createChatRoom(chatRoom, cb);
	}
	
	public List<ChatRoom> getChatRoom(ChatRoom chatRoom) {
		return service.getChatRoom(chatRoom.getName());
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
	
	public void sendMessage(String groupId, ChatMessage chatMessage, Callback<ChatMessage> cb) {
		service.sendMessage(groupId, chatMessage, cb);
	}
	
	public List<ChatMessage> getMessages(String groupId) {
		return service.getMessages(groupId);
	}
	
	public void getMessagesCb(String groupId, Callback<List<ChatMessage>> cb) {
		service.getMessagesCb(groupId, cb);
	}
}