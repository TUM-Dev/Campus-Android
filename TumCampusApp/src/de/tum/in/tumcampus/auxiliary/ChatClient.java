package de.tum.in.tumcampus.auxiliary;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.POST;
import retrofit.http.Path;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatRoom;

public class ChatClient {

	public static final String API_URL = "http://192.168.1.4:8888";
	
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
		
		@POST("/members/")
		void createMember(@Body ChatMember chatMember, Callback<ChatMember> cb);
		
		@POST("/chat_rooms/{groupId}/add_member/")
		void joinChatRoom(@Path("groupId") String groupId, @Body ChatMember chatMember, Callback<ChatRoom> cb);
		
		@DELETE("/chat_rooms/{groupId}/members/{lrzId}/")
		void leaveChatRoom(@Path("groupId") String groupdId, @Path("lrzId") String lrzId);
		
		@POST("/chat_rooms/{groupId}/messages/")
		void sendMessage(@Path("groupId") String groupId);
	}
	
	public void createGroup(ChatRoom chatRoom, Callback<ChatRoom> cb) {
		service.createChatRoom(chatRoom, cb);
	}
	
	public void createMember(ChatMember chatMember, Callback<ChatMember> cb) {
		service.createMember(chatMember, cb);
	}
	
	public void joinChatRoom(ChatRoom chatRoom, ChatMember chatMember, Callback<ChatRoom> cb) {
		String[] splitString = chatRoom.getUrl().split("/");
		String groupId = splitString[splitString.length-1];
		service.joinChatRoom(groupId, chatMember, cb);
	}
	
	public void sendMessage(String groupId) {
		service.sendMessage(groupId);
	}
}