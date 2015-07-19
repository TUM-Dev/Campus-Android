package de.tum.in.tumcampus.models;

import java.io.Serializable;

/**
 * Used for parsing the GCM json payload for the 'update' type
 */
@SuppressWarnings("UnusedDeclaration")
public class GCMUpdate implements Serializable {

    public int packageVersion;
    public int sdkVersion;
    public String releaseDate;
    public int lowestVersion;

}
