package de.tum.in.tumcampusapp.models.tumcabe;

import java.util.Date;

import de.tum.in.tumcampusapp.auxiliary.Utils;

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
    public final String link;

    /**
     * Kino Constructor
     *
     * @param id          ID
     * @param title       Title
     * @param year        Year
     * @param runtime     Runtime
     * @param genre       Genre
     * @param director    Director
     * @param actors      Actors
     * @param rating      IMDB-Rating
     * @param description Description
     * @param cover       Cover
     * @param trailer     Trailer
     * @param date        Date
     * @param created     Created
     * @param link        Link
     */
    public Kino(String id, String title, String year, String runtime, String genre, String director, String actors,
                String rating, String description, String cover, String trailer, Date date, Date created, String link) {
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
        this.link = link;
    }

    @Override
    public String toString() {
        return "id=" + id + " title=" + title + " year=" + year + " runtime=" + runtime +
               " genre=" + genre + " director=" + director + " actors=" + actors + " rating=" + rating +
               " description=" + description + " cover=" + cover + " trailer=" + trailer +
               " date=" + Utils.getDateString(date) + " created=" + Utils.getDateString(created) + " link=" + link;
    }

}

