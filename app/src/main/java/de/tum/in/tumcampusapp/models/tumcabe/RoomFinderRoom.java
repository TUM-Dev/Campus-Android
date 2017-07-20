package de.tum.in.tumcampusapp.models.tumcabe;

/**
 * This class is used as a model for rooms in Roomfinder retrofit request.
 */
public class RoomFinderRoom {
    String campus;
    String address;
    String info;
    String arch_id;
    String room_id;

    public RoomFinderRoom(String campus, String address, String info, String arch_id, String room_id) {
        this.campus = campus;
        this.address = address;
        this.info = info;
        this.arch_id = arch_id;
        this.room_id = room_id;
    }

    public String getCampus() {
        return campus;
    }

    public String getAddress() {
        return address;
    }

    public String getInfo() {
        return info;
    }

    public String getArch_id() {
        return arch_id;
    }

    public String getRoom_id() {
        return room_id;
    }
}
