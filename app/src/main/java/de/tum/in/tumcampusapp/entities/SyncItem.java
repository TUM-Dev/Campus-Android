package de.tum.in.tumcampusapp.entities;


import org.joda.time.DateTime;

import java.util.Date;

import de.tum.in.tumcampusapp.entities.converters.DateTimeConverter;
import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Generated;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;

@Entity
public class SyncItem {
    @Id
    private Long id;
    @Index
    private String identifier;
    @Convert(converter = DateTimeConverter.class, dbType = Date.class)
    private DateTime lastSync;


    public SyncItem(String identifier) {
        this.identifier = identifier;
        this.lastSync = DateTime.now();
    }

    @Generated(hash = 1065528177)
    public SyncItem(Long id, String identifier, DateTime lastSync) {
        this.id = id;
        this.identifier = identifier;
        this.lastSync = lastSync;
    }
    @Generated(hash = 44756490)
    public SyncItem() {
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public DateTime getLastSync() {
        return lastSync;
    }
    public void setLastSync(DateTime lastSync) {
        this.lastSync = lastSync;
    }
}