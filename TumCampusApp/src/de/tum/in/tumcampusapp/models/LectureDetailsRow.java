package de.tum.in.tumcampusapp.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "DetailsLehrveranstaltungen".
 * 
 * @author Daniel Mayr
 * 
 * @see http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php
 */
@Root(name = "row", strict = false)
public class LectureDetailsRow {

	@Element(required = false)
	private String dauer_info;
	// <ersttermin>Do, 05.05.2011, 13:15-14:45 N 1179,
	// Wilhelm-Nusselt-Hörsaal</ersttermin>
	@Element(required = false)
	private String ersttermin;

	// <haupt_unterrichtssprache>Deutsch</haupt_unterrichtssprache>
	@Element(required = false)
	private String haupt_unterrichtssprache;

	@Element(required = false)
	private String lehrinhalt;

	@Element(required = false)
	private String lehrmethode;
	@Element(required = false)
	private String lehrziel;
	@Element(required = false)
	private String org_kennung_betreut;
	@Element(required = false)
	private String org_name_betreut;
	@Element(required = false)
	private String org_nr_betreut;
	@Element(required = false)
	private String semester;
	@Element(required = false)
	private String semester_id;

	@Element(required = false)
	private String semester_name;
	@Element(required = false)
	private String sj_name;
	@Element(required = false)
	private String stp_lv_art_kurz;
	@Element(required = false)
	private String stp_lv_art_name;

	@Element
	private String stp_lv_nr;
	@Element
	private String stp_sp_nr;
	@Element(required = false)
	private String stp_sp_sst;
	@Element
	private String stp_sp_titel;

	// <studienbehelfe>- Skript Schaltungstechnik 1 - L.O. Chua, Ch. Desoer and
	// E. Kuh: Linear and Nonlinear Circuits</studienbehelfe>
	@Element(required = false)
	private String studienbehelfe;

	@Element(required = false)
	private String voraussetzung_lv;

	@Element(required = false)
	private String vortragende_mitwirkende;

	public String getDauer_info() {
		return dauer_info;
	}

	public String getErsttermin() {
		return ersttermin;
	}

	public String getHaupt_unterrichtssprache() {
		return haupt_unterrichtssprache;
	}

	public String getLehrinhalt() {
		return lehrinhalt;
	}

	public String getLehrmethode() {
		return lehrmethode;
	}

	public String getLehrziel() {
		return lehrziel;
	}

	public String getOrg_kennung_betreut() {
		return org_kennung_betreut;
	}

	public String getOrg_name_betreut() {
		return org_name_betreut;
	}

	public String getOrg_nr_betreut() {
		return org_nr_betreut;
	}

	public String getSemester() {
		return semester;
	}

	public String getSemester_id() {
		return semester_id;
	}

	public String getSemester_name() {
		return semester_name;
	}

	public String getSj_name() {
		return sj_name;
	}

	public String getStp_lv_art_kurz() {
		return stp_lv_art_kurz;
	}

	public String getStp_lv_art_name() {
		return stp_lv_art_name;
	}

	public String getStp_lv_nr() {
		return stp_lv_nr;
	}

	public String getStp_sp_nr() {
		return stp_sp_nr;
	}

	public String getStp_sp_sst() {
		return stp_sp_sst;
	}

	public String getStp_sp_titel() {
		return stp_sp_titel;
	}

	public String getStudienbehelfe() {
		return studienbehelfe;
	}

	public String getVoraussetzung_lv() {
		return voraussetzung_lv;
	}

	public String getVortragende_mitwirkende() {
		return vortragende_mitwirkende;
	}

	public void setDauer_info(String dauer_info) {
		this.dauer_info = dauer_info;
	}

	public void setErsttermin(String ersttermin) {
		this.ersttermin = ersttermin;
	}

	public void setHaupt_unterrichtssprache(String haupt_unterrichtssprache) {
		this.haupt_unterrichtssprache = haupt_unterrichtssprache;
	}

	public void setLehrinhalt(String lehrinhalt) {
		this.lehrinhalt = lehrinhalt;
	}

	public void setLehrmethode(String lehrmethode) {
		this.lehrmethode = lehrmethode;
	}

	public void setLehrziel(String lehrziel) {
		this.lehrziel = lehrziel;
	}

	public void setOrg_kennung_betreut(String org_kennung_betreut) {
		this.org_kennung_betreut = org_kennung_betreut;
	}

	public void setOrg_name_betreut(String org_name_betreut) {
		this.org_name_betreut = org_name_betreut;
	}

	public void setOrg_nr_betreut(String org_nr_betreut) {
		this.org_nr_betreut = org_nr_betreut;
	}

	public void setSemester(String semester) {
		this.semester = semester;
	}

	public void setSemester_id(String semester_id) {
		this.semester_id = semester_id;
	}

	public void setSemester_name(String semester_name) {
		this.semester_name = semester_name;
	}

	public void setSj_name(String sj_name) {
		this.sj_name = sj_name;
	}

	public void setStp_lv_art_kurz(String stp_lv_art_kurz) {
		this.stp_lv_art_kurz = stp_lv_art_kurz;
	}

	public void setStp_lv_art_name(String stp_lv_art_name) {
		this.stp_lv_art_name = stp_lv_art_name;
	}

	public void setStp_lv_nr(String stp_lv_nr) {
		this.stp_lv_nr = stp_lv_nr;
	}

	public void setStp_sp_nr(String stp_sp_nr) {
		this.stp_sp_nr = stp_sp_nr;
	}

	public void setStp_sp_sst(String stp_sp_sst) {
		this.stp_sp_sst = stp_sp_sst;
	}

	public void setStp_sp_titel(String stp_sp_titel) {
		this.stp_sp_titel = stp_sp_titel;
	}

	public void setStudienbehelfe(String studienbehelfe) {
		this.studienbehelfe = studienbehelfe;
	}

	public void setVoraussetzung_lv(String voraussetzung_lv) {
		this.voraussetzung_lv = voraussetzung_lv;
	}

	public void setVortragende_mitwirkende(String vortragende_mitwirkende) {
		this.vortragende_mitwirkende = vortragende_mitwirkende;
	}

}
