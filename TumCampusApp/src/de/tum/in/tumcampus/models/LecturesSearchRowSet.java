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
@Root(name = "rowset")
public class LecturesSearchRowSet {

	@ElementList(inline = true)
	private List<LecturesSearchRow> lehrveranstaltungen;

	public List<LecturesSearchRow> getLehrveranstaltungen() {
		return lehrveranstaltungen;
	}

	public void setLehrveranstaltungen(
			List<LecturesSearchRow> lehrveranstaltungen) {
		this.lehrveranstaltungen = lehrveranstaltungen;
	}

}
