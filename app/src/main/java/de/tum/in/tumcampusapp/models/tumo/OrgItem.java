package de.tum.in.tumcampusapp.models.tumo;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * An Element of the Organisation Tree. In the App a List of those Elements is
 * showed ({@link OrgItemList}). The shown Elements are for Navigation to an
 * Element without child-Element, whose details are then shown.
 */

@Root(name = "row")
public class OrgItem {

    /**
     * Organisation ID -> to identify
     */
    @Element(name = "nr")
    private String id = "";

    /**
     * German Description of the Organisation
     */
    @Element(name = "name_de")
    private String nameDe = "";

    /**
     * English Description of the Organisation
     */
    @Element(name = "name_en")
    private String nameEn = "";

    /**
     * Organisation ID of the parent Organisation
     */
    @Element(name = "parent")
    private String parentId = "";

    /**
     * Organisation ID of the parent Organisation
     */
    @Element(required = false)
    private String ebene = "";

    @Element(required = false)
    private String org_gruppe_name = "";

    @Element(required = false)
    private String child_cnt = "";

    @Element(required = false)
    private String sort_hierarchie = "";

    @Element(required = false)
    private String kennung = "";

    @Element(required = false)
    private String org_typ_name = "";

    // Getter and Setter Functions
    public String getId() {
        return id;
    }

    public String getNameDe() {
        return nameDe;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getParentId() {
        return parentId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNameDe(String name) {
        this.nameDe = name;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public void setParentId(String id) {
        this.parentId = id;
    }
}
