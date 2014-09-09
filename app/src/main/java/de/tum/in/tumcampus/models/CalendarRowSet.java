package de.tum.in.tumcampus.models;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "sucheLehrveranstaltungen" or "eigeneLehrveranstaltungen".
 * 
 * @author Daniel Mayr
 * @see LecturesSearchRow
 * @see http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php
 * @review Thomas Behrens
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
