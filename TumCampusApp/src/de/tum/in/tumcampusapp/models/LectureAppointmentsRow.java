package de.tum.in.tumcampusapp.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "TermineLehrveranstaltungen".
 * 
 * @author Daniel Mayr
 * 
 * @see http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php
 */
@Root(name = "row")
public class LectureAppointmentsRow {

	@Element(required = false)
	private String art;

	@Element
	private String beginn_datum_zeitpunkt;

	@Element
	private String ende_datum_zeitpunkt;

	@Element(required = false)
	private String ort;

	@Element(required = false)
	private String raum_nr;

	@Element(required = false)
	private String raum_nr_architekt;

	@Element(required = false)
	private String termin_betreff;

	public String getArt() {
		return art;
	}

	public String getBeginn_datum_zeitpunkt() {
		return beginn_datum_zeitpunkt;
	}

	public String getEnde_datum_zeitpunkt() {
		return ende_datum_zeitpunkt;
	}

	public String getOrt() {
		return ort;
	}

	/*
	 * Florian Schulz added TODO Review Vasyl
	 */
	public String getRaum_nr() {
		return raum_nr;
	}

	public String getRaum_nr_architekt() {
		return raum_nr_architekt;
	}

	public String getTermin_betreff() {
		return termin_betreff;
	}

	public void setArt(String art) {
		this.art = art;
	}

	public void setBeginn_datum_zeitpunkt(String beginn_datum_zeitpunkt) {
		this.beginn_datum_zeitpunkt = beginn_datum_zeitpunkt;
	}

	public void setEnde_datum_zeitpunkt(String ende_datum_zeitpunkt) {
		this.ende_datum_zeitpunkt = ende_datum_zeitpunkt;
	}

	public void setOrt(String ort) {
		this.ort = ort;
	}

	public void setRaum_nr(String raum_nr) {
		this.raum_nr = raum_nr;
	}

	public void setRaum_nr_architekt(String raum_nr_architekt) {
		this.raum_nr_architekt = raum_nr_architekt;
	}

	public void setTermin_betreff(String termin_betreff) {
		this.termin_betreff = termin_betreff;
	}

}