package de.tum.in.tumcampusapp.entities;


import org.joda.time.DateTime;

import java.util.Date;

import de.tum.in.tumcampusapp.entities.converters.DateTimeConverter;
import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Generated;
import io.objectbox.annotation.Id;

@Entity
public class TumLock {
    @Id
    private Long id;

    private String url;
    private String error;

    @Convert(converter = DateTimeConverter.class, dbType = Date.class)
    private DateTime timestamp;
    private int lockedFor;

    @Generated(hash = 23807323)
    public TumLock(Long id, String url, String error, DateTime timestamp,
            int lockedFor) {
        this.id = id;
        this.url = url;
        this.error = error;
        this.timestamp = timestamp;
        this.lockedFor = lockedFor;
    }
    @Generated(hash = 1289217145)
    public TumLock() {
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }
    public DateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }
    public int getLockedFor() {
        return lockedFor;
    }
    public void setLockedFor(int lockedFor) {
        this.lockedFor = lockedFor;
    }

    
}