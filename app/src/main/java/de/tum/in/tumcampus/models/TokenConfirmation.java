package de.tum.in.tumcampus.models;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@SuppressWarnings("UnusedDeclaration")
@Root(name = "confirmation")
public class TokenConfirmation {
    @Text
	private String confirmation;

	public boolean isConfirmed() {
		return confirmation.equals("true");
	}
}
