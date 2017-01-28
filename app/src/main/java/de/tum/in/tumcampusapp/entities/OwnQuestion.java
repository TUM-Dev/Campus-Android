package de.tum.in.tumcampusapp.entities;


import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

import de.tum.in.tumcampusapp.entities.converters.DateTimeConverter;
import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Generated;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Relation;
import io.objectbox.annotation.apihint.Internal;
import io.objectbox.exception.DbDetachedException;
import io.objectbox.exception.DbException;

@Entity
public class OwnQuestion {
    @Id
    private Long id;

    @Index
    private Integer question;
    private String text;
    @Relation(idProperty = "faculty")
    public List<Faculty> targetFac;
    @Convert(converter = DateTimeConverter.class, dbType = Date.class)
    private DateTime created;
    @Convert(converter = DateTimeConverter.class, dbType = Date.class)
    private DateTime end;
    private Integer yes = 0;
    private Integer no = 0;
    private Boolean deleted = false;
    private Boolean synced = false;

    /** Used to resolve relations */
    @Internal
    @Generated(hash = 1307364262)
    transient BoxStore __boxStore;


    public OwnQuestion(Integer question, String text, List<Faculty> targetFac, DateTime created, DateTime end, Integer yes, Integer no, Boolean deleted, Boolean synced) {
        this.question = question;
        this.text = text;
        this.targetFac = targetFac;
        this.created = created;
        this.end = end;
        this.yes = yes;
        this.no = no;
        this.deleted = deleted;
        this.synced = synced;
    }

    @Generated(hash = 402062532)
    public OwnQuestion(Long id, Integer question, String text, DateTime created, DateTime end, Integer yes, Integer no, Boolean deleted, Boolean synced) {
        this.id = id;
        this.question = question;
        this.text = text;
        this.created = created;
        this.end = end;
        this.yes = yes;
        this.no = no;
        this.deleted = deleted;
        this.synced = synced;
    }

    @Generated(hash = 332061035)
    public OwnQuestion() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuestion() {
        return question;
    }

    public void setQuestion(Integer question) {
        this.question = question;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    public DateTime getEnd() {
        return end;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }

    public Integer getYes() {
        return yes;
    }

    public void setYes(Integer yes) {
        this.yes = yes;
    }

    public Integer getNo() {
        return no;
    }

    public void setNo(Integer no) {
        this.no = no;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getSynced() {
        return synced;
    }

    public void setSynced(Boolean synced) {
        this.synced = synced;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1095782774)
    public List<Faculty> getTargetFac() {
        if (targetFac == null) {
            final BoxStore boxStore = this.__boxStore;
            if (boxStore == null) {
                throw new DbDetachedException();
            }
            Box<Faculty> box = boxStore.boxFor(Faculty.class);
            int targetEntityId = boxStore.getEntityIdOrThrow(Faculty.class);
            List<Faculty> targetFacNew = box.getBacklinkEntities(targetEntityId, Faculty_.faculty, id);
            synchronized (this) {
                if (targetFac == null) {
                    targetFac = targetFacNew;
                }
            }
        }
        return targetFac;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 554821294)
    public synchronized void resetTargetFac() {
        targetFac = null;
    }

    /**
     * Removes entity from its object box. Entity must attached to an entity context.
     */
    @Generated(hash = 2074427333)
    public void remove() {
        if (__boxStore == null) {
            throw new DbDetachedException();
        }
        __boxStore.boxFor(OwnQuestion.class).remove(this);
    }

    /**
     * Puts the entity in its object box.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 814888196)
    public void put() {
        if (__boxStore == null) {
            throw new DbDetachedException();
        }
        __boxStore.boxFor(OwnQuestion.class).put(this);
    }

}