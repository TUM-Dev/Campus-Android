package de.tum.in.tumcampusapp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.component.ui.chat.ChatMessageValidator;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMessage;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatPublicKey;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestApp.class)
public class ChatMessageValidatorTestCase {

    private ChatMessageValidator validator;
    private List<ChatPublicKey> publicKeyFixtures;
    private List<ChatMessage> messageFixtures;

    @Before
    public void setUp() {
        publicKeyFixtures = new ArrayList<>();
        publicKeyFixtures.add(new ChatPublicKey(
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDR4+3zbRYVRAecvMcn0vLswZAI1z7JqQ2Q0Mkq\n" +
                "ZAy78cE/tja8qcD4DXXiQYCKC8BdI68W+DqYLohPuOs6rTYfD/pLsbPKaJLHEb4dw0Uchq36pb60\n" +
                "6G6aCjZrYM0JJYO/pKbwl6ceF6EJRacGswUQ8qY3ZYd6W7R3J7MQxzJ+lQIDAQAB"));

        messageFixtures = new ArrayList<>();
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
                "This is a Korean message: \uC88B\uC740 \uAC8C\uC784",
                "L6DPFzKiVWrO3TeAjJwPNtC4U5D69ODloH3zmCXCpZx+fiZhopzC5cUAeolm2l/++KYZu3vR6IJK\n" +
                "HLjXaMd4jDruY8DiWNmCbOnR/ywHQ96sCuMcdfhot5AgM05NbBH7GiAFBDJQzDejuK7M7hGmHZ6s\n" +
                "L4WYKETiNiP1Oc6d58w="));
    }

    /**
     * A helper method which builds a {@link List} instance based on the public key fixtures.
     *
     * @param start The index of the first element to include in the list
     * @param end   The index after the last element to include in the list
     */
    private List<ChatPublicKey> buildPubkeyList(int start, int end) {
        List<ChatPublicKey> list = new ArrayList<>();
        for (int i = start; i < end; ++i) {
            list.add(publicKeyFixtures.get(i));
        }

        return list;
    }

    private ChatMessage buildChatMessage(String text, String signature) {
        ChatMessage message = new ChatMessage(text);
        message.setSignature(signature);

        return message;
    }

    /**
     * Tests that a valid ASCII-based message is correctly found as valid when
     * there is only one public key associated to the validator.
     */
    @Test
    public void testAsciiValidMessageOneKey() {
        validator = new ChatMessageValidator(buildPubkeyList(0, 1));
        ChatMessage message = messageFixtures.get(0);

        assertTrue(validator.validate(message));
    }

    /**
     * Tests that the validator finds an invalid signature attached to a message.
     * The signature is still a valid base64 string.
     */
    @Test
    public void testAsciiInvalidMessageOneKey() {
        validator = new ChatMessageValidator(buildPubkeyList(0, 1));
        ChatMessage message = messageFixtures.get(0);
        // Take a signature of a different message
        message.setSignature(messageFixtures.get(1)
                                            .getSignature());

        assertFalse(validator.validate(message));
    }

    /**
     * Tests that a unicode (european) message is correctly validated.
     */
    @Test
    public void testUnicodeValidMessageOneKey() {
        validator = new ChatMessageValidator(buildPubkeyList(0, 1));
        ChatMessage message = messageFixtures.get(1);

        assertTrue(validator.validate(message));
    }

    /**
     * Tests that a unicode (korean) message is correctly validated.
     */
    @Test
    public void testUnicodeKoreanValidMessageOneKey() {
        validator = new ChatMessageValidator(buildPubkeyList(0, 1));
        ChatMessage message = messageFixtures.get(2);

        assertTrue(validator.validate(message));
    }

    /**
     * Tests that when the signature is not a valid base64 string, the validator
     * simply says the message is not valid.
     */
    @Test
    public void testInvalidBase64Signature() {
        validator = new ChatMessageValidator(buildPubkeyList(0, 1));
        ChatMessage message = buildChatMessage("This is a message!", "This is not valid base64...");

        assertFalse(validator.validate(message));
    }

    /**
     * Tests that when the public key associated with the validator is not a valid
     * base64 string, the validator simply says the message is not valid.
     */
    @Test
    public void testInvalidBase64PublicKey() {
        List<ChatPublicKey> list = buildPubkeyList(0, 1);
        list.get(0)
            .setKey("This is not valid base 64");
        validator = new ChatMessageValidator(list);
        ChatMessage message = messageFixtures.get(0);

        assertFalse(validator.validate(message));
    }

    /**
     * Tests that when the validator does not have any public keys associated to it,
     * no message is found valid.
     */
    @Test
    public void testNoPublicKeys() {
        validator = new ChatMessageValidator(new ArrayList<>());

        for (ChatMessage message : messageFixtures) {
            assertFalse(validator.validate(message));
        }
    }

    /**
     * Tests that a message is found valid regardless of the fact that there is one invalid
     * key in the list of public keys.
     */
    @Test
    public void testOneInvalidKey() {
        List<ChatPublicKey> list = new ArrayList<>();
        list.add(new ChatPublicKey("This is not a valid key"));
        list.add(publicKeyFixtures.get(0));
        validator = new ChatMessageValidator(list);
        ChatMessage message = messageFixtures.get(0);

        assertTrue(validator.validate(message));
    }

}
