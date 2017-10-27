package de.tum.in.tumcampusapp.models.tumcabe;

import android.support.annotation.NonNull;

import de.tum.in.tumcampusapp.adapters.SimpleStickyListHeadersAdapter;

/**
 * Presents the faculty model that is used in fetching the facultyData from server
 */
public class Curriculum implements Comparable<Curriculum>, SimpleStickyListHeadersAdapter.SimpleStickyListItem {
    private String curriculum; // id
    private String category;
    private String name;
    private String url;

    public Curriculum(String curriculum, String category, String name, String url) {
        this.curriculum = curriculum;
        this.category = category;
        this.name = name;
        this.url = url;
    }

    public String getCurriculum() {
        return curriculum;
    }

    public void setCurriculum(String curriculum) {
        this.curriculum = curriculum;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getHeadName() {
        return (category.charAt(0) + "").toUpperCase() + category.substring(1, category.length());
    }

    @Override
    public String getHeaderId() {
        return category;
    }

    @Override
    public int compareTo(@NonNull Curriculum curriculum) {
        if (category.equals(curriculum.getCategory())) {
            return name.compareTo(curriculum.getName());
        } else {
            return category.compareTo(curriculum.getCategory());
        }
    }
}