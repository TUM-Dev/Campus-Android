package de.tum.in.tumcampusapp.models.cards;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import de.tum.in.tumcampusapp.BR;


public class StudyCard extends BaseObservable {

    private int id;
    private int member;
    private int lecture;
    private int card_type;
    private String title;
    private String front_text;
    private String front_image;
    private String back_text;
    private String back_image;
    private boolean can_shift;

    public StudyCard() {
        // TODO remove default values for lecture (eg use -1 as default and user has to select a lecture)
        this.setLecture(1);
        this.setCard_type(1);
        this.setTitle("");
        this.setFront_text("");
        this.setFront_image("");
        this.setBack_text("");
        this.setBack_image("");
        this.setCan_shift(false);
    }

    public StudyCard(int id, int member, int lecture, String title, String front_text, String front_image, String back_text, String back_image, boolean can_shift) {
        this.setId(id);
        this.setMember(member);
        this.setLecture(lecture);
        this.setTitle(title);
        this.setFront_text(front_text);
        this.setFront_image(front_image);
        this.setBack_text(back_text);
        this.setBack_image(back_image);
        this.setCan_shift(can_shift);
    }

    @Bindable
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }

    @Bindable
    public int getLecture() {
        return lecture;
    }

    public void setLecture(int lecture) {
        this.lecture = lecture;
        notifyPropertyChanged(BR.lecture);
    }

    @Bindable
    public int getMember() {
        return member;
    }

    public void setMember(int member) {
        this.member = member;
        notifyPropertyChanged(BR.member);
    }

    @Bindable
    public int getCard_type() {
        return card_type;
    }

    public void setCard_type(int card_type) {
        this.card_type = card_type;
        notifyPropertyChanged(BR.card_type);
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getFront_text() {
        return front_text;
    }

    public void setFront_text(String front_text) {
        this.front_text = front_text;
        notifyPropertyChanged(BR.front_text);
    }

    @Bindable
    public String getFront_image() {
        return front_image;
    }

    public void setFront_image(String front_image) {
        this.front_image = front_image;
        notifyPropertyChanged(BR.front_image);
    }

    @Bindable
    public String getBack_text() {
        return back_text;
    }

    public void setBack_text(String back_text) {
        this.back_text = back_text;
        notifyPropertyChanged(BR.back_text);
    }

    @Bindable
    public String getBack_image() {
        return back_image;
    }

    public void setBack_image(String back_image) {
        this.back_image = back_image;
        notifyPropertyChanged(BR.back_image);
    }

    @Bindable
    public boolean isCan_shift() {
        return can_shift;
    }

    public void setCan_shift(boolean can_shift) {
        this.can_shift = can_shift;
        notifyPropertyChanged(BR.can_shift);
    }

    public boolean is_valid() {
        // TODO
        return !this.getTitle().isEmpty() && this.getLecture() != -1;
    }

    @Override
    public String toString() {
        return this.getTitle();
    }
}
