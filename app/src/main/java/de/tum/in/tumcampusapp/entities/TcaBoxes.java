package de.tum.in.tumcampusapp.entities;


import android.content.Context;

import io.objectbox.BoxStore;

public class TcaBoxes {

    private static TcaBoxes instance = null;
    private BoxStore bs;

    private TcaBoxes(Context c) {

        //Build & attach the boxstore
        bs = MyObjectBox.builder().androidContext(c).build();
    }

    public static TcaBoxes init(Context c) {
        if (instance != null) {
            return instance;
        }

        instance = new TcaBoxes(c);
        return instance;
    }

    public static BoxStore getBoxStore() {
        return instance.bs;
    }
}
