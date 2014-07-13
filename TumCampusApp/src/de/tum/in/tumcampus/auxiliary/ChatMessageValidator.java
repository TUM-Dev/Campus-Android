package de.tum.in.tumcampus.auxiliary;

import java.util.ArrayList;

import de.tum.in.tumcampus.models.ChatMessage2;
import de.tum.in.tumcampus.models.ChatPublicKey;

public class ChatMessageValidator {
	
	
	public boolean validate(ChatMessage2 message) {
		// Generate hash of the received message
		
		// Get the user's public key
		ArrayList<ChatPublicKey> publicKeys = (ArrayList<ChatPublicKey>) ChatClient.getInstance().getPublicKeysForMember(message.getMember().getUserId());
		
		// Decrypt the received signature using the user's well-known public key
		
		// Compare the decrypted hash with the generated hash
		
		return true;
	}
	
	
}
