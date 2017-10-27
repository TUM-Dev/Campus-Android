package de.tum.in.tumcampusapp.models.tumo;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * This class is dealing with the de-serialization of the output of TUMOnline to the method "sucheLehrveranstaltungen".
 */

@Root(name = "rowset")
public class OrgDetailItemList {

    /**
     * List of OrganisationItems
     */
    @ElementList(inline = true, required = false)
    private List<OrgDetailsItem> groups;

    public List<OrgDetailsItem> getGroups() {
        return groups;
    }
}
