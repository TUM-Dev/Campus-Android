package de.tum.in.tumcampusapp.models.tumo;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "DetailsLehrveranstaltungen"
 *
 * @see LectureDetailsRow
 * @see <a href="http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php">SimpleXML tutorial</a>
 */
@Root(name = "rowset")
public class LectureDetailsRowSet {

    @ElementList(inline = true)
    private List<LectureDetailsRow> lehrveranstaltungenDetails;

    public List<LectureDetailsRow> getLehrveranstaltungenDetails() {
        return lehrveranstaltungenDetails;
    }

    public void setLehrveranstaltungenDetails(List<LectureDetailsRow> lehrveranstaltungenDetails) {
        this.lehrveranstaltungenDetails = lehrveranstaltungenDetails;
    }
}
