package de.tum.in.tumcampusapp.models.gcm;

import java.io.Serializable;

/**
 * Used for parsing the GCM json payload for the 'alert' type
 */
public class GCMAlert implements Serializable {

    private static final long serialVersionUID = 3906290674499996501L;
    public boolean silent;

}
