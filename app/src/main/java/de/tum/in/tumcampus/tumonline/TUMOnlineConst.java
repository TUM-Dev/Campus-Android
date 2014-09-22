package de.tum.in.tumcampus.tumonline;

/**
 * Enum for all TUMOnline access possibilities
 */
public enum TUMOnlineConst {
    CALENDER("kalender"),
    STUDIENBEITRAGSTATUS("studienbeitragsstatus"),
    LECTURES_PERSONAL("veranstaltungenEigene"),
    LECTURES_DETAILS("veranstaltungenDetails"),
    LECTURES_APPOINTMENTS("veranstaltungenTermine"),
    LECTURES_SEARCH("veranstaltungenSuche"),
    ORG_TREE("orgBaum"),
    ORG_DETAILS("orgDetails"),
    PERSONEN_DETAILS("personenDetails"),
    PERSONEN_SUCHE("personenSuche"),
    NOTEN("noten"),
    TOKEN_CONFIRMED("isTokenConfirmed"),
    REQUEST_TOKEN("requestToken");
    private final String stringValue;
    private TUMOnlineConst(final String s) { stringValue = s; }
    public String toString() { return stringValue; }
}