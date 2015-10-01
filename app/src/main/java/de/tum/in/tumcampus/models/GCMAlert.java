package de.tum.in.tumcampus.models;

import java.io.Serializable;

/**
 * Used for parsing the GCM json payload for the 'alert' type
 */
@SuppressWarnings("UnusedDeclaration")
public class GCMAlert implements Serializable {

    public boolean silent;

}
