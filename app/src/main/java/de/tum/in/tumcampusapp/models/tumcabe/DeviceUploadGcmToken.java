package de.tum.in.tumcampusapp.models.tumcabe;

import android.content.Context;

import de.tum.in.tumcampusapp.auxiliary.AuthenticationManager;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;

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
