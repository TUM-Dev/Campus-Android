package de.tum.in.tumcampusapp.models;


import java.io.Serializable;

/**
 * Used for parsing the GCM json payload for the 'chat' type
 */
public class GCMChat implements Serializable {
    private static final long serialVersionUID = -3920974316634829667L;
    public int room;
    public int member;
    public int message;
}
