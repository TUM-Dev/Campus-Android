package de.tum.in.tumcampusapp.models.tumcabe;

import android.content.Context;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

import de.tum.in.tumcampusapp.auxiliary.AuthenticationManager;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;

public class ChatVerification {

    private String signature;
    private final String date;
    private final String rand;
    private final int member;
    private Object data;

    public ChatVerification(Context c, ChatMember member) throws NoPrivateKey {
        //Create some data
        this.date = new Date().toString();
        this.rand = new BigInteger(130, new SecureRandom()).toString(32);
        this.member = member.getId();

        //Sign this data for verification
        AuthenticationManager am = new AuthenticationManager(c);
        this.signature = am.sign(date + rand + member.getLrzId());
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setData(Object o) {
        this.data = o;
    }
}
