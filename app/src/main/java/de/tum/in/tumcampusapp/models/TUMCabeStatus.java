package de.tum.in.tumcampusapp.models;

import android.content.Context;

import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;

public class TUMCabeStatus {

    private String status;

    public TUMCabeStatus(Context c) throws NoPrivateKey {

    }

    public String getStatus(){
        return this.status;
    }

}
