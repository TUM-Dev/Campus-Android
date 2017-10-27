package de.tum.in.tumcampusapp.models.tumcabe;

import java.io.Serializable;

import de.tum.in.tumcampusapp.adapters.SimpleStickyListHeadersAdapter.SimpleStickyListItem;

/**
 * This class is used as a model for rooms in Roomfinder retrofit request.
 */
public class RoomFinderRoom implements SimpleStickyListItem, Serializable {
    private static final long serialVersionUID = 6631656320611471476L;

    String campus;
    String address;
    String info;
    String arch_id;
    String room_id;
    String name;    // Campus name

    public RoomFinderRoom(String campus, String address, String info, String arch_id, String room_id, String name) {
        this.campus = campus;
        this.address = address;
        this.info = info;
        this.arch_id = arch_id;
        this.room_id = room_id;
        this.name = name;
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

    public String getName() {
        if (name != null && name.equals("null")) {
            return "";
        }
        return name;
    }

    @Override
    public String getHeadName() {
        return getName();
    }

    @Override
    public String getHeaderId() {
        return getHeadName();
    }

    @Override
    public String toString() {
        return getCampus() + ";" + getAddress() + ";"
               + getInfo() + ";" + getArch_id() + ";"
               + getRoom_id() + ";" + getName();
    }
}
