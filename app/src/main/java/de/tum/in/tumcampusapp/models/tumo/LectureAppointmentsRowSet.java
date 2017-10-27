package de.tum.in.tumcampusapp.models.tumo;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "TermineLehrveranstaltungen".
 *
 * @see LectureAppointmentsRow
 * @see <a href="http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php">SimpleXML tutorial</a>
 */
@Root(name = "rowset")
public class LectureAppointmentsRowSet {

    @ElementList(inline = true, required = false)
    private List<LectureAppointmentsRow> lehrveranstaltungenTermine;

    public List<LectureAppointmentsRow> getLehrveranstaltungenTermine() {
        return lehrveranstaltungenTermine;
    }

    public void setLehrveranstaltungenTermine(List<LectureAppointmentsRow> lehrveranstaltungenTermine) {
        this.lehrveranstaltungenTermine = lehrveranstaltungenTermine;
    }
}
