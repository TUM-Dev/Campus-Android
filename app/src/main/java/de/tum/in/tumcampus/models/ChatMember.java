package de.tum.in.tumcampus.models;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("UnusedDeclaration")
public class ChatMember {

	private String url = null;
	@SerializedName("lrz_id")
	private String lrzId;
	@SerializedName("display_name")
	private String displayName;
	private String signature;

	public ChatMember(String lrzId) {
		super();
		this.lrzId = lrzId;
	}
	
	public ChatMember(String url, String lrzId, String displayName) {
		super();
		this.url = url;
		this.lrzId = lrzId;
		this.displayName = displayName;
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
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	public String getUserId() {
		String[] splitString = getUrl().split("/");
		return splitString[splitString.length-1];
	}
}
