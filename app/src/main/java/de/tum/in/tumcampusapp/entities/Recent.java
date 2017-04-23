package de.tum.in.tumcampusapp.entities;


import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Generated;
import io.objectbox.annotation.Id;

@Entity
public class Recent {
    @Id
    private Long id;

    private Integer typ;
    private String name;


    public Recent(Integer typ, String name) {
        this.typ = typ;
        this.name = name;
    }

    @Generated(hash = 657659363)
    public Recent(Long id, Integer typ, String name) {
        this.id = id;
        this.typ = typ;
        this.name = name;
    }

    @Generated(hash = 1212650171)
    public Recent() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTyp() {
        return typ;
    }

    public void setTyp(Integer typ) {
        this.typ = typ;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}