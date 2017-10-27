package de.tum.in.tumcampusapp.models.tumo;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "error", strict = false)
public class Error {

    @Element(name = "message")
    private String message = "";

    // Getter and Setter Functions
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
