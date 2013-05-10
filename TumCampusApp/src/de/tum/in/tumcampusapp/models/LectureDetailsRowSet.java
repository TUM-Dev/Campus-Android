package de.tum.in.tumcampusapp.models;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "DetailsLehrveranstaltungen"
 * 
 * @author Daniel Mayr
 * @see LectureDetailsRow
 * @see http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php
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
