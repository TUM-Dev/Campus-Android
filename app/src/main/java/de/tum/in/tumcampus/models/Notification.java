package de.tum.in.tumcampus.models;

public class Notification {
    private int notification;
    private int type;
    private NotificationLocation location;
    private String title;
    private String description;
    private String signature;

    public Notification(int notification, int type, NotificationLocation location, String title, String description, String signature) {
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

    public NotificationLocation getLocation() {
        return location;
    }

    public void setLocation(NotificationLocation location) {
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
}
