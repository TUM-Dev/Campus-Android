package de.tum.in.tumcampus.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * A room that belongs to some {@link Person} or {@link Employee}. Note: This
 * model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Root(name = "raum", strict = false)
public class Room implements Serializable {

    private static final long serialVersionUID = -5328581629897735774L;

    @Element(name = "ortsbeschreibung")
    private String location = "";

    @Element(name = "kurz")
    private String number = "";

    public String getLocation() {
        return location;
    }

    public String getNumber() {
        return number;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
