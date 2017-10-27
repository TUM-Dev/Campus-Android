package de.tum.in.tumcampusapp.models.tumo;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "sucheLehrveranstaltungen" or "eigeneLehrveranstaltungen".
 *
 * @see LecturesSearchRow
 */
@Root(name = "rowset")
public class LecturesSearchRowSet {

    @ElementList(inline = true, required = false)
    private List<LecturesSearchRow> lehrveranstaltungen = new ArrayList<>();

    public List<LecturesSearchRow> getLehrveranstaltungen() {
        return lehrveranstaltungen;
    }

    public void setLehrveranstaltungen(List<LecturesSearchRow> lehrveranstaltungen) {
        this.lehrveranstaltungen = lehrveranstaltungen;
    }
}
