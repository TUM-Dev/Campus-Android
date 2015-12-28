package de.tum.in.tumcampus.models;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "token")
public class AccessToken {
    @Text
    private String token = "";

    public String getToken() {
        return token;
    }
}
