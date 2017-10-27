package de.tum.in.tumcampusapp.models.tumcabe;

import java.util.Date;

import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * News Object
 */
public class News {

    /**
     * News Facebook-ID
     */
    public final String id;

    /**
     * Local image, e.g. /mnt/sdcard/tumcampus/news/cache/xy.jpg
     */
    public final String image;

    /**
     * Link Url, e.g. http://www.in.tum.de
     */
    public final String link;

    /**
     * Content
     */
    public final String title;

    /**
     * Date
     */
    public final Date date;
    public final Date created;

    public final String src;

    /**
     * New News
     *
     * @param id      News Facebook-ID
     * @param title   Title
     * @param link    Url, e.g. http://www.in.tum.de
     * @param image   Image url e.g. http://www.tu-film.de/img/film/poster/Fack%20ju%20Ghte.jpg
     * @param date    Date
     * @param created Creation date
     */
    public News(String id, String title, String link, String src, String image, Date date, Date created) {
        this.id = id;
        this.title = title;
        this.link = link;
        this.src = src;
        this.image = image;
        this.date = date;
        this.created = created;
    }

    @Override
    public String toString() {
        return "id=" + id + " title=" + title + " link=" + link + " image="
               + image + " date=" + Utils.getDateString(date) + " created=" + Utils.getDateString(created);
    }
}