package de.tum.in.tumcampus.models;

import com.google.gson.annotations.SerializedName;

public class ChatPublicKey {

	private String url;
	@SerializedName("key_text")
	private String key;
	private boolean active;
	
	public ChatPublicKey(String key) {
		this.key = key;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
