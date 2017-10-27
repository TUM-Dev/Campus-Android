package de.tum.in.tumcampusapp.models.tumcabe;

import com.google.gson.annotations.SerializedName;

public class ChatRegistrationId {

    @SerializedName("registration_id")
    private String regId;
    private String status;
    private String signature;

    public ChatRegistrationId(String regId, String signature) {
        this.regId = regId;
        this.signature = signature;
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
