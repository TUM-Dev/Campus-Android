package de.tum.in.tumcampusapp.models;

import java.io.Serializable;

/**
 * Used for parsing the GCM json payload for the 'update' type
 */
public class GCMUpdate implements Serializable {

    private static final long serialVersionUID = -4597228673980239217L;
    public int packageVersion;
    public int sdkVersion;
    public String releaseDate;
    public int lowestVersion;

}
