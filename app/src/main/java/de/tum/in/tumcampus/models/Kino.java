package de.tum.in.tumcampus.models;

import java.util.Date;

import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Kino Object
 */
public class Kino {

    // all entries in the kino database
    public final String id;
    public final String title;
    public final String year;
    public final String runtime;
    public final String genre;
    public final String director;
    public final String actors;
    public final String rating;
    public final String description;
    public final String cover;
    public final String trailer;
    public final Date date;
    public final Date created;

    /**
     * Kino Constructor
     * @param id
     * @param title
     * @param year
     * @param runtime
     * @param genre
     * @param director
     * @param actors
     * @param rating
     * @param description
     * @param cover
     * @param trailer
     * @param date
     * @param created
     */
    public Kino(String id, String title, String year, String runtime, String genre, String director, String actors,
                String rating, String description, String cover, String trailer, Date date, Date created){
        this.id = id;
        this.title = title;
        this.year = year;
        this.runtime = runtime;
        this.genre = genre;
        this.director = director;
        this.actors = actors;
        this.rating = rating;
        this.description = description;
        this.cover = cover;
        this.trailer = trailer;
        this.date = date;
        this.created = created;
    }

    public String toString(){
        return "id=" + id + " title=" + title + " year=" + year + " runtime=" + runtime +
                " genre=" + genre + " director=" + director + " actors=" + actors + " rating=" + rating +
                " description=" + description + " cover=" + cover + " trailer=" + trailer +
                " date=" + Utils.getDateString(date) + " created=" + Utils.getDateString(created);
    }

}

