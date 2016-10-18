package de.tum.in.tumcampusapp.models.tumcabe;

import android.content.Context;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

import de.tum.in.tumcampusapp.auxiliary.AuthenticationManager;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;

public class DeviceVerification {

    private String signature;
    private String date;
    private String rand;
    private String device;

    public DeviceVerification(Context c) throws NoPrivateKey {
        //Create some data
        this.date = (new Date()).toString();
        this.rand = new BigInteger(130, new SecureRandom()).toString(32);
        this.device = AuthenticationManager.getDeviceID(c);

        //Sign this data for verification
        AuthenticationManager am = new AuthenticationManager(c);
        this.signature = am.sign(date + rand + this.device);
    }

}
