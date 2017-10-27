package de.tum.in.tumcampusapp.models.cafeteria;

/**
 * Location object
 */
public class Location {

    /**
     * Address
     */
    public final String address;

    /**
     * Category
     */
    public final String category;

    /**
     * Opening hours
     */
    public final String hours;

    /**
     * Location ID
     */
    public final int id;

    /**
     * Location name
     */
    public final String name;

    /**
     * Remark
     */
    public final String remark;

    /**
     * Room
     */
    public final String room;

    /**
     * Next transport station
     */
    public final String transport;

    /**
     * URL
     */
    public final String url;

    /**
     * New Location
     *
     * @param id        Location ID, e.g. 100
     * @param category  Location category, e.g. library, cafeteria, info
     * @param name      Location name, e.g. Studentenwerksbibliothek
     * @param address   Address, e.g. Arcisstr. 21
     * @param room      Room, e.g. MI 00.01.123
     * @param transport Transportation station name, e.g. U2 Königsplatz
     * @param hours     Opening hours, e.g. Mo–Fr 8–24
     * @param remark    Additional information, e.g. Tel: 089-11111
     * @param url       Location URL, e.g. http://stud.ub.uni-muenchen.de/
     */
    public Location(int id, String category, String name, String address,
                    String room, String transport, String hours, String remark,
                    String url) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.address = address;
        this.room = room;
        this.transport = transport;
        this.hours = hours;
        this.remark = remark;
        this.url = url;
    }

    @Override
    public String toString() {
        return "id=" + id + ", category=" + category + ", name=" + name
               + ", address=" + address + ", room=" + room + ", transport="
               + transport + ", hours=" + hours + ", remark=" + remark
               + ", url=" + url;
    }
}