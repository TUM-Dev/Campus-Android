package de.tum.in.tumcampusapp.models.barrierfree;

/**
 * The model used to display more infromation in barrier free page
 */
public class BarrierfreeMoreInfo {
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
}
