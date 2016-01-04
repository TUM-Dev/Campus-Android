package de.tum.in.tumcampus.models;

import android.content.Context;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

import de.tum.in.tumcampus.auxiliary.AuthenticationManager;
import de.tum.in.tumcampus.exceptions.NoPrivateKey;

public class DeviceRegister {

    private String signature;
    private String date;
    private String rand;
    private String device;
    private String publicKey;

    public DeviceRegister(Context c, String publickey) throws NoPrivateKey {
        //Create some data
        this.date = (new Date()).toString();
        this.rand = new BigInteger(130, new SecureRandom()).toString(32);
        this.device = AuthenticationManager.getDeviceID(c);
        this.publicKey = publickey;

        //Sign this data for verification
        AuthenticationManager am = new AuthenticationManager(c);
        this.signature = am.sign(date + rand + this.device);
    }

}
