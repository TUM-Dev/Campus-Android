package de.tum.in.tumcampus.cards;

import android.content.Context;

public interface ProvidesCard {
    public void onRequestCard(Context context) throws Exception;
}
