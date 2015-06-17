package de.tum.in.tumcampus.models;

/**
 * Created by enricogiga on 16/06/2015.
 */
public class MVVDeparture extends  MVVObject {
    public String line;
    public String direction;
    public Integer min;

    public MVVDeparture(String line, String direction, Integer min) {
        this.line = line;
        this.direction = direction;
        this.min = min;
    }
}
