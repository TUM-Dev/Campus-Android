package de.tum.in.tumcampusapp.models;

public class GCMNotificationLocation {
    private int location;
    private String name;
    private double lon;
    private double lat;
    private int radius;

    public GCMNotificationLocation(int location, String name, double lon, double lat, int radius) {
        this.location = location;
        this.name = name;
        this.lon = lon;
        this.lat = lat;
        this.radius = radius;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
