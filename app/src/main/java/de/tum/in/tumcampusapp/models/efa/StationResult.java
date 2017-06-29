package de.tum.in.tumcampusapp.models.efa;

public class StationResult {
    public final String station;
    public final String id;
    public final int quality;

    public StationResult(String station, String id, int quality) {
        this.station = station;
        this.id = id;
        this.quality = quality;
    }
}