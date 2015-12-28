package de.tum.in.tumcampus.models;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "confirmed")
public class TokenConfirmation {
    @Text
    private String confirmed = "";

    public boolean isConfirmed() {
        return confirmed.equals("true");
    }
}
