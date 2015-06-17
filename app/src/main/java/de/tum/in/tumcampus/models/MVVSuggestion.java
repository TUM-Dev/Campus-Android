package de.tum.in.tumcampus.models;

import de.tum.in.tumcampus.R;


/**
 * Created by enricogiga on 16/06/2015.
 * Model to hold a suggestion from the suggested stop list of mvv when query is not fully correct
 */

public class MVVSuggestion extends  MVVObject {

    public static String baseURL="http://www.mvg-live.de";
    public String link;
    public String name;

    public MVVSuggestion(String link, String name) {

        this.link = baseURL + link;
        this.name = name;

    }
}
