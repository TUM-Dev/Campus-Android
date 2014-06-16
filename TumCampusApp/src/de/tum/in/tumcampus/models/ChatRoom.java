package de.tum.in.tumcampus.models;

import java.util.ArrayList;

public class ChatRoom {
	
	private String messages;
	private String url;
	private String name;
	private ArrayList<String> members = new ArrayList<String>();
	
	public ChatRoom(String name) {
		super();
		this.name = name;
	}
	
	public String getMessages() {
		return messages;
	}
	public void setMessages(String messages) {
		this.messages = messages;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<String> getMembers() {
		return members;
	}
	public void setMembers(ArrayList<String> members) {
		this.members = members;
	}
}
