package de.tum.in.tumcampus.models;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Element;

@SuppressWarnings("UnusedDeclaration")
@Root(name = "row")
public class Identity {
    @Element
    private String vorname;

    @Element
    private String familienname;

    @Element
    public String kennung;

    @Override
    public String toString() {
        return vorname+" "+familienname;
    }
}
