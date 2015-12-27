package de.tum.in.tumcampus.models;

import android.content.Context;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.Date;

import de.tum.in.tumcampus.auxiliary.AuthenticationManager;
import de.tum.in.tumcampus.auxiliary.RSASigner;
import de.tum.in.tumcampus.exception.NoPublicKey;

public class ChatVerification {

    private String signature;
    private String date;
    private String rand;
    private int member;
    private Object data;

    public ChatVerification(Context c, ChatMember member) throws NoPublicKey {
        //Create some data
        this.date = (new Date()).toString();
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

    public void setData(Object o){
        this.data=o;
    }
}
