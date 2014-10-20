package de.tum.in.tumcampus.models;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.Date;

import de.tum.in.tumcampus.auxiliary.RSASigner;

@SuppressWarnings("UnusedDeclaration")
public class ChatVerification {

    private String signature;
    private String date;
    private String rand;
    private String member;

    public ChatVerification(PrivateKey pk, ChatMember member) {
        //Create some data
        this.date = (new Date()).toString();
        this.rand = new BigInteger(130, new SecureRandom()).toString(32);
        this.member = member.getId();

        //Sign this data for verification
        RSASigner signer = new RSASigner(pk);
        this.signature = signer.sign(date + rand + member.getLrzId());
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
