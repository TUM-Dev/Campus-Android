package de.tum.in.tumcampus.models;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Wrapper class holding a list of exams.
 * <p>
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrens
 */
@Root(name = "rowset")
public class ExamList {

	@ElementList(inline = true)
	private List<Exam> grades;

	public List<Exam> getExams() {
		return grades;
	}

	public void setExams(List<Exam> grades) {
		this.grades = grades;
	}

}
