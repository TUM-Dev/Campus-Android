package de.tum.in.tumcampus.models;

import com.google.gson.annotations.SerializedName;

public class ChatRegistrationId {

	@SerializedName("registration_id")
	private String regId;
	
	public ChatRegistrationId(String regId) {
		this.regId = regId;
	}

	public String getRegId() {
		return regId;
	}

	public void setRegId(String regId) {
		this.regId = regId;
	}
}
