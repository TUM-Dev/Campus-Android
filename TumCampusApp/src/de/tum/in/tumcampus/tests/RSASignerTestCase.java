package de.tum.in.tumcampus.tests;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;

import junit.framework.TestCase;
import android.util.Base64;
import de.tum.in.tumcampus.auxiliary.RSASigner;
import de.tum.in.tumcampus.models.ListChatMessage;

public class RSASignerTestCase extends TestCase {
	
	private RSASigner signer;
	private PrivateKey privateKeyFixture;
	private ArrayList<ListChatMessage> messageFixtures;
	
	
	private PrivateKey buildPrivateKey(String privateKeyString) {
		byte[] privateKeyBytes = Base64.decode(privateKeyString, Base64.DEFAULT);
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			return keyFactory.generatePrivate(privateKeySpec);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	protected void setUp() throws Exception {
		privateKeyFixture = buildPrivateKey(
			"MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAM46wq9uOF7y1dmNO3nY8D1P6bCe" +
			"t3izsm2GKQtvWmV78WbBxk1rZI2GNExvZ3aVg4mb6jOToGzm+jdNiWR07kBFSlrgNC5zq7Jmm0gz" +
			"yTgrou6eV5NoQsPO2fI3tworvxgGa886Hu0U8p3gFk+BJnffndSq8DdBKcIjGDunTv4dAgMBAAEC" +
			"gYB3QywLX+ZhonVhVneqw3ZLPseaSG858lGhXRCneEICpma4Uh9n7k88OPxNp69huJ1VG0GZiiog" +
			"UIMrMD/gRG7y2NJxKkDbR2aVB+YR6aGAWYfwZVDM+Swxe3wUbDfRsAuXGapKCXnjHOKGbUCi/gx9" +
			"AvFH4YJ9IImFGD+T7jSogQJBAP/oBCSXhDCHQXoN3vqdncmNNKl1J9QvzPY2ESMzfbdJ4QESb4YZ" +
			"f88ixXUZpb3JVamW4WI6QHEHGMnGZwJyL6UCQQDOThaq2Mw/8QyPiKup7B+QT7/Bz7wpQx7IHw6a" +
			"r5pX5AYAO7WmihxOgDMX6VfhCJlksxfLTVSuiJdXuJSm90sZAkBdXuJkF4R70F3rkrQQ7QFtUMAu" +
			"NDjcCrTWANQv69Gq1qHqKjfWzeb8RMuW9kyq+pLu1cZWeLqaguRgequLEO6hAkBsq1NjUOldsQI1" +
			"xP7vdbI2mNtgIqVxcqqPLVTLBD6flzvV+Z24iL1aWEsRiFdC8P2jvnaFH0nA2bAmg9LBlDdZAkEA" +
			"9tg4rD0aCHQZ6kEquwN6emc9QM0X6DR0dx6Bqq8CGDkVdk0hXHBR9VUBGE4YSsxpn+LnyWSWyJum" +
			"dWuepeUKig==");
		
		messageFixtures = new ArrayList<ListChatMessage>();
		messageFixtures.add(buildChatMessage(
			"This is a message!",
			"MwBZFVhzIGehiGAVaoxp0k04BJN8YyyqlPQg1hXwg1bQxgjtEXz6KsVzYOWo40/TdhcbUHo+hUhk\n" +
			"/rLLBrkFldQuNGhd/ltwiMeN2KwdLYm5nl9DWIjPXXviCBogkVtwrBdAhgknr5Kn5Zy4TbGdMr9z\n" +
			"d/iOl27L7GYepazgNW8="));
		messageFixtures.add(buildChatMessage(
			"A message with German characters: öäüßÖÄÜ!",
			"qCOAmFho4tKW24qn6vv6j0x4jc3OVKPbVVm7EqYeJKBStGOmEcx6Crtx0MEMFxNe4Zyqo0kYMXNO\n" +
			"/NPTvhUJlAr5x6Hlc1iKWBT5eGE8F3mKE8pTSObrCWhEBXylQjkwej5eQpahW+uexZWzeme702V0\n" +
			"1C3FoeYUC9rSOfLlyss="));
		messageFixtures.add(buildChatMessage(
			"This is a Korean message: 좋은 게임",
			"L6DPFzKiVWrO3TeAjJwPNtC4U5D69ODloH3zmCXCpZx+fiZhopzC5cUAeolm2l/++KYZu3vR6IJK\n" +
			"HLjXaMd4jDruY8DiWNmCbOnR/ywHQ96sCuMcdfhot5AgM05NbBH7GiAFBDJQzDejuK7M7hGmHZ6s\n" +
			"L4WYKETiNiP1Oc6d58w="));
	}
	
	protected ListChatMessage buildChatMessage(String text, String signature) {
		ListChatMessage message = new ListChatMessage(text);
		message.setSignature(signature);
		
		return message;
	}
	
	/**
	 * Tests that a valid ASCII-based message is correctly signed.
	 */
	public void testAsciiMessageSigning() {
		signer = new RSASigner(privateKeyFixture);
		ListChatMessage message = messageFixtures.get(0);
		
		assertEquals(message.getSignature(), signer.sign(message.getText()));
	}
	
	/**
	 * Tests that a unicode (european) message is correctly signed.
	 */
	public void testUnicodeMessageSigning() {
		signer = new RSASigner(privateKeyFixture);
		ListChatMessage message = messageFixtures.get(1);
		
		assertEquals(message.getSignature(), signer.sign(message.getText()));
	}
	
	/**
	 * Tests that a unicode (korean) message is correctly signed.
	 */
	public void testUnicodeKoreanMessageSigning() {
		signer = new RSASigner(privateKeyFixture);
		ListChatMessage message = messageFixtures.get(2);
		
		assertEquals(message.getSignature(), signer.sign(message.getText()));
	}
	
	/**
	 * Tests that when the private key associated with 
	 * the signer is null, the signer returns null.
	 */
	public void testPrivateKeyNull() {
		signer = new RSASigner(null);
		ListChatMessage message = messageFixtures.get(0);
		
		assertNull(signer.sign(message.getText()));
	}
}
