package de.tum.in.tumcampusapp.models.tumo;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "sucheLehrveranstaltungen" or "eigeneLehrveranstaltungen".
 *
 * @see LecturesSearchRow
 * @see <a href="http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php">SimpleXML tutorial</a>
 */
@Root(name = "events")
public class CalendarRowSet {

    @ElementList(inline = true, required = false)
    private List<CalendarRow> kalendarList;

    public List<CalendarRow> getKalendarList() {
        return kalendarList;
    }

    public void setKalendarList(List<CalendarRow> kalendarList) {
        this.kalendarList = kalendarList;
    }

}
