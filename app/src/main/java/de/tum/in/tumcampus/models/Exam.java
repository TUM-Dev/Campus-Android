package de.tum.in.tumcampus.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Exam passed by the user.
 * <p/>
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Root(name = "row", strict = false)
public class Exam {

    @Element(name = "lv_titel")
    private String course = "";

    @Element(name = "lv_credits")
    private String credits = "";

    @Element(name = "datum", required = false)
    private String date = "";

    @Element(name = "pruefer_nachname", required = false)
    private String examiner = "";

    @Element(name = "uninotenamekurz")
    private String grade = "";

    @Element(name = "modus", required = false)
    private String modus = "";

    @Element(name = "studienidentifikator")
    private String programID = "";

    @Element(name = "lv_semester", required = false)
    private String semester = "";

    public String getCourse() {
        return course;
    }

    public String getCredits() {
        return credits;
    }

    public String getDate() {
        return date;
    }

    public String getExaminer() {
        return examiner;
    }

    public String getGrade() {
        return grade;
    }

    public String getModus() {
        return modus;
    }

    public String getProgramID() {
        return programID;
    }

    public String getSemester() {
        return semester;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }
}