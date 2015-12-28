package de.tum.in.tumcampus.models;

import android.content.Context;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

import de.tum.in.tumcampus.auxiliary.AuthenticationManager;
import de.tum.in.tumcampus.exceptions.NoPrivateKey;

public class DeviceUploadGcmToken {

    private DeviceVerification verification;
    private String token;
    private String signature;

    public DeviceUploadGcmToken(Context c, String token) throws NoPrivateKey {
        //Create some data
        this.token = token;
        this.verification = new DeviceVerification(c);

        //Sign this data for verification
        AuthenticationManager am = new AuthenticationManager(c);
        this.signature = am.sign(token);
    }

}
