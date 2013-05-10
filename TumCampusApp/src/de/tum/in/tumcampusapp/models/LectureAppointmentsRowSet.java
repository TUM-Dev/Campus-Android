package de.tum.in.tumcampusapp.models;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "TermineLehrveranstaltungen".
 * 
 * @author Daniel Mayr
 * @see LectureAppointmentsRow
 * @see http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php
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
