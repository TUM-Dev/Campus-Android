package de.tum.in.tumcampusapp.models.barrierfree;

import de.tum.in.tumcampusapp.adapters.SimpleStickyListHeadersAdapter;

/**
 * The model used to display more infromation in barrier free page
 */
public class BarrierfreeMoreInfo implements SimpleStickyListHeadersAdapter.SimpleStickyListItem{
//    private int id;
    private String title;
    private String category;
    private String url;

    public BarrierfreeMoreInfo(String title, String category, String url) {
        this.title = title;
        this.category = category;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String getHeadName() {
        return getCategory();
    }
}
