package de.tum.in.tumcampus.models;

import android.content.Context;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

import de.tum.in.tumcampus.auxiliary.AuthenticationManager;
import de.tum.in.tumcampus.exceptions.NoPrivateKey;

public class TUMCabeStatus {

    private String status;

    public TUMCabeStatus(Context c) throws NoPrivateKey {

    }

    public String getStatus(){
        return this.status;
    }

}
