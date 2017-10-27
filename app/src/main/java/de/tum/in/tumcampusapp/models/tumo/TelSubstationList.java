package de.tum.in.tumcampusapp.models.tumo;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.List;

/**
 * Wrapper class holding a list of {@link TelSubstation}s. Note: This model is
 * based on the TUMOnline web service response format for a corresponding
 * request.
 */
@Root(name = "telefon_nebenstellen")
public class TelSubstationList implements Serializable {

    private static final long serialVersionUID = -3790189526859194869L;

    @ElementList(inline = true, required = false)
    private List<TelSubstation> substations;

    public List<TelSubstation> getSubstations() {
        return substations;
    }

    public void setSubstations(List<TelSubstation> substations) {
        this.substations = substations;
    }

}
