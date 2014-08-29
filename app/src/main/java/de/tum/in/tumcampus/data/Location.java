package de.tum.in.tumcampus.data;


public class Location {
    public String content;
    public String id;

    private double lon;
    private double lat;

    public Location(String id, String content) {
        this.id = id;
        this.content = content;
    }

    @Override
    public String toString() {
        return content;
    }

    public double distanceTo(Location other) {
        double d2r = Math.PI / 180;
        double distance = 0;

        double dlong = (this.lon - other.lon) * d2r;
        double dlat = (this.lat - other.lat) * d2r;
        double a = Math.pow(Math.sin(dlat / 2.0), 2)
                + Math.cos(this.lat * d2r)
                * Math.cos(other.lat * d2r)
                * Math.pow(Math.sin(dlong / 2.0), 2);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        //Multiply by radius to get distance
        return 6367.0 * c;


    }
}
