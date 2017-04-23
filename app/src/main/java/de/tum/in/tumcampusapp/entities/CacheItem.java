package de.tum.in.tumcampusapp.entities;


import org.joda.time.DateTime;

import java.util.Date;

import de.tum.in.tumcampusapp.entities.converters.DateTimeConverter;
import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Generated;
import io.objectbox.annotation.Id;

@Entity
public class CacheItem {
    @Id
    private Long id;

    private String url;
    private String data;
    private int validity;

    @Convert(converter = DateTimeConverter.class, dbType = Date.class)
    private DateTime maxAge;
    private int typ;


    public CacheItem(String url, String data, int validity, DateTime maxAge, int typ) {
        this.url = url;
        this.data = data;
        this.validity = validity;
        this.maxAge = maxAge;
        this.typ = typ;
    }


    @Generated(hash = 1650764347)
    public CacheItem(Long id, String url, String data, int validity, DateTime maxAge,
                     int typ) {
        this.id = id;
        this.url = url;
        this.data = data;
        this.validity = validity;
        this.maxAge = maxAge;
        this.typ = typ;
    }


    @Generated(hash = 508180393)
    public CacheItem() {
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


    public String getData() {
        return data;
    }


    public void setData(String data) {
        this.data = data;
    }


    public int getValidity() {
        return validity;
    }


    public void setValidity(int validity) {
        this.validity = validity;
    }


    public DateTime getMaxAge() {
        return maxAge;
    }


    public void setMaxAge(DateTime maxAge) {
        this.maxAge = maxAge;
    }


    public int getTyp() {
        return typ;
    }


    public void setTyp(int typ) {
        this.typ = typ;
    }


}