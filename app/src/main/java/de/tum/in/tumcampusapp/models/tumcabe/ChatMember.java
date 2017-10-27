package de.tum.in.tumcampusapp.models.tumcabe;

import com.google.gson.annotations.SerializedName;

public class ChatMember {

    private int id;
    @SerializedName("lrz_id")
    private String lrzId;
    @SerializedName("display_name")
    private String displayName;
    private String signature;

    public ChatMember(String lrzId) {
        super();
        this.lrzId = lrzId;
    }

    public ChatMember(int id, String lrzId, String displayName) {
        super();
        this.id = id;
        this.lrzId = lrzId;
        this.displayName = displayName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}
