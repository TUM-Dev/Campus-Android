package de.tum.in.tumcampus.models;

/**
 * Used for parsing the GCM json payload for the 'alert' type
 */
@SuppressWarnings("UnusedDeclaration")
public class GCMAlert {

    public String title;
    public String text;
    public String signature;
    public boolean sound;
    public boolean fake;

}
