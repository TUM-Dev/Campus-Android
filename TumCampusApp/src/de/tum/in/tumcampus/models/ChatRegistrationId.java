package de.tum.in.tumcampus.models;

import com.google.gson.annotations.SerializedName;

public class ChatRegistrationId {

	@SerializedName("registration_id")
	private String regId;
	private String status;

	public ChatRegistrationId(String regId) {
		this.regId = regId;
	}

	public String getRegId() {
		return regId;
	}

	public void setRegId(String regId) {
		this.regId = regId;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
