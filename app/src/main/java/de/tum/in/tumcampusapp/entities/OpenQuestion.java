package de.tum.in.tumcampusapp.entities;


import org.joda.time.DateTime;

import java.util.Date;

import de.tum.in.tumcampusapp.entities.converters.DateTimeConverter;
import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Generated;

@Entity
public class OpenQuestion {
    @Id
    private Long id;

    @Index
    private Integer question;
    private String text;
    @Convert(converter = DateTimeConverter.class, dbType = Date.class)
    private DateTime created;
    @Convert(converter = DateTimeConverter.class, dbType = Date.class)
    private DateTime end;
    private Integer answer;
    private Boolean answered;
    private Boolean synced;

    public OpenQuestion(Integer question, String text, DateTime created, DateTime end) {
        this.question = question;
        this.text = text;
        this.created = created;
        this.end = end;
        this.answer = 0;
        this.answered = false;
        this.synced = false;
    }

    @Generated(hash = 122344648)
    public OpenQuestion(Long id, Integer question, String text, DateTime created, DateTime end, Integer answer, Boolean answered, Boolean synced) {
        this.id = id;
        this.question = question;
        this.text = text;
        this.created = created;
        this.end = end;
        this.answer = answer;
        this.answered = answered;
        this.synced = synced;
    }
    @Generated(hash = 1554699869)
    public OpenQuestion() {
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
    public Integer getAnswer() {
        return answer;
    }
    public void setAnswer(Integer answer) {
        this.answer = answer;
    }
    public Boolean getAnswered() {
        return answered;
    }
    public void setAnswered(Boolean answered) {
        this.answered = answered;
    }
    public Boolean getSynced() {
        return synced;
    }
    public void setSynced(Boolean synced) {
        this.synced = synced;
    }
}