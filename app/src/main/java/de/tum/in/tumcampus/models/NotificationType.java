package de.tum.in.tumcampus.models;

@SuppressWarnings("UnusedDeclaration")
public class NotificationType {
    private int type;
    private String name;
    private String icon;
    private boolean silent;
    private boolean confirmation;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public boolean isConfirmation() {
        return confirmation;
    }

    public void setConfirmation(boolean confirmation) {
        this.confirmation = confirmation;
    }
}
