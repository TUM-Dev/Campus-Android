package de.tum.in.tumcampusapp.models.tumcabe;

public class ChatRoom {

    private String name;
    private int id;

    private int members = -1;

    public ChatRoom(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMembers() {
        return members;
    }

    public void setMembers(int members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return id + ": " + name;
    }
}
