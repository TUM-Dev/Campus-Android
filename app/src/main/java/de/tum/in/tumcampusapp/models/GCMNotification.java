package de.tum.in.tumcampusapp.models;

import java.io.Serializable;

public class GCMNotification implements Serializable {
    private static final long serialVersionUID = 8643117662605459731L;
    private int notification;
    private int type;
    private GCMNotificationLocation location;
    private String title;
    private String description;
    private String signature;
    private String created;

    public GCMNotification(int notification, int type, GCMNotificationLocation location, String title, String description, String signature) {
        this.notification = notification;
        this.type = type;
        this.location = location;
        this.title = title;
        this.description = description;
        this.signature = signature;
    }

    public int getNotification() {
        return notification;
    }

    public void setNotification(int notification) {
        this.notification = notification;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public GCMNotificationLocation getLocation() {
        return location;
    }

    public void setLocation(GCMNotificationLocation location) {
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getCreated() {
        return this.created;
    }
}
