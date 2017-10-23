package de.tum.in.tumcampusapp.models.efa;

public class StationResult {
    public String station;
    public String id;
    public int quality;

    @SuppressWarnings("unused")
    public StationResult() {
        // no-args constructor for GSON
    }

    public StationResult(String station, String id, int quality) {
        this.station = station;
        this.id = id;
        this.quality = quality;
    }

    @Override
    public String toString() {
        return station;
    }
}