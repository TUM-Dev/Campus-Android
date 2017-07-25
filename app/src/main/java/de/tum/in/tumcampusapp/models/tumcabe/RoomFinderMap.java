package de.tum.in.tumcampusapp.models.tumcabe;

/**
 * This class is used as a model for maps in Roomfinder retrofit request.
 */
public class RoomFinderMap {
    String map_id;
    String description;

    public RoomFinderMap(String map_id, String description) {
        this.map_id = map_id;
        this.description = description;
    }

    public String getMap_id() {
        return map_id;
    }

    public String getDescription() {
        return description;
    }
}
