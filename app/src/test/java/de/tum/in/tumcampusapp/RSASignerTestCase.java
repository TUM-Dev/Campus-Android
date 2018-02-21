package de.tum.in.tumcampusapp;

import android.util.Base64;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.api.app.AuthenticationManager;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMessage;
import de.tum.in.tumcampusapp.utils.RSASigner;
import de.tum.in.tumcampusapp.utils.Utils;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class RSASignerTestCase {

    private RSASigner signer;
    private PrivateKey privateKeyFixture;
    private List<ChatMessage> messageFixtures;

    private static PrivateKey buildPrivateKey(String privateKeyString) {
        byte[] privateKeyBytes = Base64.decode(privateKeyString, Base64.DEFAULT);
        KeyFactory keyFactory;
        try {
            keyFactory = AuthenticationManager.getKeyFactoryInstance();
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            return keyFactory.generatePrivate(privateKeySpec);
        } catch (InvalidKeySpecException e) {
            Utils.log(e);
        }
        return null;
    }

    @Before
    public void setUp() {
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

        messageFixtures = new ArrayList<>();
        messageFixtures.add(buildChatMessage(
                "This is a message!",
                "Tw7Geajto7C/orsLT4TfNCUa1gnu6pGumfp+Nck7/QoOmDxilgQCpuzlpxa5Y7xuQ2rQB4XhFSm4\n" +
                "3gOHijTwF91SQx2sdIWClofzr/H0JABpQRkkMbsVQikwOnQYp+d9c1eylNPeendoYW4NAEBKpNyw\n" +
                "ShtHN6jcC2Usw1lAfxE=\n"));
        messageFixtures.add(buildChatMessage(
                "A message with German characters: öäüßÖÄÜ!",
                "tSHKrusEPatW7CUJGbPjLfpPkoO/hQnJPMCQDztVjQJNqpEk+Jbm+FTwakOQ49OaMtmZTfnKUoJQ\n" +
                "MBwgp/I6zL7Xlafxiw+jA72ah/kvixm46VlpGFF2sEYfC0Ts3Agyq1T7XXYgkrGKjC3vs6sGNFGv\n" +
                "IefIoEAOGaWIfZnnbuM=\n"));
        messageFixtures.add(buildChatMessage(
                "This is a Korean message: \uC88B\uC740 \uAC8C\uC784",
                "Td+E1WOg5FweCrBKzsjjVbbf3EeiNLu/PWID1Tg41ak5NFllqsFUcPEzPP0bZ+Dpv0sU7deQ9BaQ\n" +
                "lNVaNClQsI7Y5jTmoqS5elRdrig+eq9Qzl7bvEr0EI5CUvwLZJU4LCpLYUJEGD++IOzE0xZxB6/j\n" +
                "MES0525W5YVR0knzoKw=\n"));
    }

    private ChatMessage buildChatMessage(String text, String signature) {
        ChatMessage message = new ChatMessage(text);
        message.setSignature(signature);
        return message;
    }

    /**
     * Tests that a valid ASCII-based message is correctly signed.
     */
    @Test
    public void testAsciiMessageSigning() {
        signer = new RSASigner(privateKeyFixture);
        ChatMessage message = messageFixtures.get(0);

        assertThat(signer.sign(message.getText())).isEqualTo(message.getSignature());
    }

    /**
     * Tests that a unicode (european) message is correctly signed.
     */
    @Test
    public void testUnicodeMessageSigning() {
        signer = new RSASigner(privateKeyFixture);
        ChatMessage message = messageFixtures.get(1);

        assertThat(signer.sign(message.getText())).isEqualTo(message.getSignature());
    }

    /**
     * Tests that a unicode (korean) message is correctly signed.
     */
    @Test
    public void testUnicodeKoreanMessageSigning() {
        signer = new RSASigner(privateKeyFixture);
        ChatMessage message = messageFixtures.get(2);

        assertThat(signer.sign(message.getText())).isEqualTo(message.getSignature());
    }

    /**
     * Tests that when the private key associated with
     * the signer is null, the signer returns null.
     */
    @Test
    public void testPrivateKeyNull() {
        signer = new RSASigner(null);
        ChatMessage message = messageFixtures.get(0);

        assertThat(signer.sign(message.getText())).isNull();
    }
}
