package de.tum.in.tumcampus.models;

import de.tum.in.tumcampus.R;


/**
 * Created by enricogiga on 16/06/2015.
 * Model to hold a suggestion from the suggested stop list of mvv when query is not fully correct
 */

public class MVVSuggestion extends  MVVObject {

    private static String baseURL="http://www.mvg-live.de";
    private String link;
    private String name;

    public MVVSuggestion(String link, String name) {
        this.isSuggestion = true;
        this.link = baseURL + link;
        this.name = name;

    }
    public static String getBaseURL() {
        return baseURL;
    }

    public static void setBaseURL(String baseURL) {
        MVVSuggestion.baseURL = baseURL;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
