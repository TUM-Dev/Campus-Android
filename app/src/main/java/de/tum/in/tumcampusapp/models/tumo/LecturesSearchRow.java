package de.tum.in.tumcampusapp.models.tumo;

import android.support.annotation.NonNull;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import de.tum.in.tumcampusapp.adapters.SimpleStickyListHeadersAdapter;

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "sucheLehrveranstaltungen".
 */

@Root(name = "row")
public class LecturesSearchRow
        implements Comparable<LecturesSearchRow>, SimpleStickyListHeadersAdapter.SimpleStickyListItem {

    public static final String STP_SP_NR = "stp_sp_nr";

    @Element
    private String dauer_info = "";
    // <dauer_info>4</dauer_info>

    @Element(required = false)
    private String org_kennung_betreut = "";
    // <org_kennung_betreut>TUEIEDA</org_kennung_betreut>

    @Element(required = false)
    private String org_name_betreut = "";
    // <org_name_betreut>Lehrstuhl für Entwurfsautomatisierung (Prof.
    // Schlichtmann)</org_name_betreut>

    @Element(required = false)
    private int org_nr_betreut;
    // <org_nr_betreut>15393</org_nr_betreut>

    @Element
    private String semester = "";
    // <semester>S</semester>

    @Element
    private String semester_id = "";
    // <semester_id>11S</semester_id>

    @Element
    private String semester_name = "";
    // <semester_name>Sommersemester>2011</semester_name>

    @Element
    private String sj_name = "";
    // <sj_name>2010/11</sj_name>

    @Element
    private String stp_lv_art_kurz = "";
    // <stp_lv_art_kurz>PR</stp_lv_art_kurz>

    @Element
    private String stp_lv_art_name = "";
    // <stp_lv_art_name>Praktikum</stp_lv_art_name>

    @Element(name = "stp_lv_nr")
    private String stp_lv_nr = "";
    // lehrveranstaltungsnummer

    @Element
    private String stp_sp_nr = "";
    // <stp_sp_nr>950006549</stp_sp_nr>

    @Element
    private String stp_sp_sst = "";
    // <stp_sp_sst>4</stp_sp_sst>

    @Element(name = "stp_sp_titel")
    private String titel = "";
    // <stp_sp_titel>Praktikum (Rechnergestützte)
    // Schaltungssimulation</stp_sp_titel>

    @Element(required = false)
    private String vortragende_mitwirkende = "";

    // <vortragende_mitwirkende>Schlichtmann U, Pehl M
    // [L]</vortragende_mitwirkende>

    public String getDauer_info() {
        return dauer_info;
    }

    public String getOrg_kennung_betreut() {
        return org_kennung_betreut;
    }

    public String getOrg_name_betreut() {
        return org_name_betreut;
    }

    public int getOrg_nr_betreut() {
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

    public String getTitel() {
        return titel;
    }

    public String getVortragende_mitwirkende() {
        return vortragende_mitwirkende;
    }

    @Override
    public int compareTo(@NonNull LecturesSearchRow lecturesSearchRow) {
        return lecturesSearchRow.getSemester_id()
                                .compareTo(semester_id);
    }

    @Override
    public String getHeadName() {
        return getSemester_name();
    }

    @Override
    public String getHeaderId() {
        return getSemester_id();
    }
}