package de.tum.in.tumcampusapp.entities;


import android.content.Context;

import io.objectbox.BoxStore;

public class MyBoxStore {

    private static MyBoxStore instance = null;
    private BoxStore bs;

    private MyBoxStore(Context c) {

        //Build & attach the boxstore
        bs = MyObjectBox.builder().androidContext(c).build();
    }

    public static MyBoxStore init(Context c) {
        if (instance != null) {
            return instance;
        }

        instance = new MyBoxStore(c);
        return instance;
    }

    public static BoxStore getBoxStore() {
        return instance.bs;
    }
}
