package de.tum.in.tumcampus.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * Telefon substation to reach an employee. Note: This model is based on the
 * TUMOnline web service response format for a corresponding request.
 */
@Root(name = "nebenstelle", strict = false)
public class TelSubstation implements Serializable {

    private static final long serialVersionUID = -3289209179488515142L;

    @Element(name = "telefonnummer")
    private String number = "";

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
