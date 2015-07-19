package de.tum.in.tumcampus.models;


import java.io.Serializable;

/**
 * Used for parsing the GCM json payload for the 'chat' type
 */
@SuppressWarnings("UnusedDeclaration")
public class GCMChat implements Serializable {
    public int room;
    public int member;
    public int message;
}
