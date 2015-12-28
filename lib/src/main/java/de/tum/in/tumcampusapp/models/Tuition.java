package de.tum.in.tumcampusapp.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Class holding tuition information.
 * <p/>
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Root(name = "row", strict = false)
public class Tuition {

    @Element(name = "frist")
    private String frist = "";

    @Element(name = "semester_bezeichnung")
    private String semester_bezeichnung = "";

    @Element(name = "soll")
    private String soll = "";

    public String getFrist() {
        return frist;
    }

    public String getSemesterBez() {
        return semester_bezeichnung;
    }

    public String getSoll() {
        return soll;
    }

}
