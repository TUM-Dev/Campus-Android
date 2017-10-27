package de.tum.in.tumcampusapp.models.tumo;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.List;

/**
 * Wrapper class holding a list of {@link Room}s. Note: This model is based on
 * the TUMOnline web service response format for a corresponding request.
 */
@Root(name = "raeume")
public class RoomList implements Serializable {

    private static final long serialVersionUID = 1115343203243361774L;

    @ElementList(inline = true, required = false)
    private List<Room> rooms;

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

}
