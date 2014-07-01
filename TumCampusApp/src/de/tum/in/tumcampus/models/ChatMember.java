package de.tum.in.tumcampus.models;

import com.google.gson.annotations.SerializedName;

public class ChatMember {

	private String url = null;
	@SerializedName("lrz_id")
	private String lrzId;
	@SerializedName("first_name")
	private String displayName;
	
	public ChatMember(String lrzId) {
		super();
		this.lrzId = lrzId;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getLrzId() {
		return lrzId;
	}
	public void setLrzId(String lrzId) {
		this.lrzId = lrzId;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getUserId() {
		String[] splitString = getUrl().split("/");
		return splitString[splitString.length-1];
	}
}
