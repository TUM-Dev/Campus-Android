package de.tum.in.tumcampusapp.models.tumo;

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
