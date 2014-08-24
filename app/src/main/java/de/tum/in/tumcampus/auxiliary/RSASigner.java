package de.tum.in.tumcampus.auxiliary;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

import android.util.Base64;

/**
 * Class providing an API to generate signatures of strings.
 * 
 * Takes care of handling all unicode juggling and crypto algorithm selection.
 * 
 */
public class RSASigner {
	/**
	 * A {@link PrivateKey} instance which will be used to generate the signature.
	 */
	private PrivateKey privateKey;

	public RSASigner(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	/**
	 * Sign the message given as the parameter and return it as a base64 encoded
	 * {@link String}.
	 * 
	 * @param message
	 *            The message to be encoded
	 * @return A base64 encoded signature
	 */
	public String sign(String message) {
		Signature signer = null;
		try {
			signer = Signature.getInstance("SHA1WithRSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}

		try {
			signer.initSign(privateKey);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return null;
		}

		byte[] messageBytes = null;
		try {
			messageBytes = message.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		try {
			signer.update(messageBytes);
		} catch (SignatureException e) {
			e.printStackTrace();
			return null;
		}

		byte[] signature = null;
		try {
			signature = signer.sign();
		} catch (SignatureException e) {
			e.printStackTrace();
			return null;
		}

        return Base64.encodeToString(
                signature,
                Base64.DEFAULT);
	}
}
