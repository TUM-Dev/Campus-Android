package de.tum.`in`.tumcampusapp.component.ui.chat

import android.support.test.runner.AndroidJUnit4
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMessage
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatPublicKey
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class ChatMessageValidatorTestCase {

    private lateinit var validator: ChatMessageValidator
    private lateinit var publicKeyFixtures: MutableList<ChatPublicKey>
    private lateinit var messageFixtures: MutableList<ChatMessage>

    @Before
    fun setUp() {
        publicKeyFixtures = mutableListOf(
                ChatPublicKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDR4+3zbRYVRAecvMcn0vLswZAI1z7JqQ2Q0Mkq\n" +
                                "ZAy78cE/tja8qcD4DXXiQYCKC8BdI68W+DqYLohPuOs6rTYfD/pLsbPKaJLHEb4dw0Uchq36pb60\n" +
                                "6G6aCjZrYM0JJYO/pKbwl6ceF6EJRacGswUQ8qY3ZYd6W7R3J7MQxzJ+lQIDAQAB")
        )

        messageFixtures = mutableListOf(
                buildChatMessage(
                        "This is a message!",
                        "MwBZFVhzIGehiGAVaoxp0k04BJN8YyyqlPQg1hXwg1bQxgjtEXz6KsVzYOWo40/TdhcbUHo+hUhk\n" +
                                "/rLLBrkFldQuNGhd/ltwiMeN2KwdLYm5nl9DWIjPXXviCBogkVtwrBdAhgknr5Kn5Zy4TbGdMr9z\n" +
                                "d/iOl27L7GYepazgNW8="
                ),
                buildChatMessage(
                        "A message with German characters: öäüßÖÄÜ!",
                        "qCOAmFho4tKW24qn6vv6j0x4jc3OVKPbVVm7EqYeJKBStGOmEcx6Crtx0MEMFxNe4Zyqo0kYMXNO\n" +
                                "/NPTvhUJlAr5x6Hlc1iKWBT5eGE8F3mKE8pTSObrCWhEBXylQjkwej5eQpahW+uexZWzeme702V0\n" +
                                "1C3FoeYUC9rSOfLlyss="
                ),
                buildChatMessage(
                        "This is a Korean message: \uC88B\uC740 \uAC8C\uC784",
                        "L6DPFzKiVWrO3TeAjJwPNtC4U5D69ODloH3zmCXCpZx+fiZhopzC5cUAeolm2l/++KYZu3vR6IJK\n" +
                                "HLjXaMd4jDruY8DiWNmCbOnR/ywHQ96sCuMcdfhot5AgM05NbBH7GiAFBDJQzDejuK7M7hGmHZ6s\n" +
                                "L4WYKETiNiP1Oc6d58w="
                )
        )
    }

    /**
     * A helper method which builds a [List] instance based on the public key fixtures.
     *
     * @param start The index of the first element to include in the list
     * @param end   The index after the last element to include in the list
     */
    private fun buildPublicKeyList(start: Int, end: Int): List<ChatPublicKey> {
        return (start until end)
                .map { publicKeyFixtures[it] }
                .toList()
    }

    private fun buildChatMessage(text: String, signature: String): ChatMessage {
        return ChatMessage(text).apply {
            this.signature = signature
        }
    }

    /**
     * Tests that a valid ASCII-based message is correctly found as valid when
     * there is only one public key associated to the validator.
     */
    @Test
    fun testAsciiValidMessageOneKey() {
        validator = ChatMessageValidator(buildPublicKeyList(0, 1))
        val message = messageFixtures.first()
        assertTrue(validator.validate(message))
    }

    /**
     * Tests that the validator finds an invalid signature attached to a message.
     * The signature is still a valid base64 string.
     */
    @Test
    fun testAsciiInvalidMessageOneKey() {
        validator = ChatMessageValidator(buildPublicKeyList(0, 1))
        val message = messageFixtures.first().apply {
            // Take a signature of a different message
            signature = messageFixtures[1].signature
        }

        assertFalse(validator.validate(message))
    }

    /**
     * Tests that a unicode (european) message is correctly validated.
     */
    @Test
    fun testUnicodeValidMessageOneKey() {
        validator = ChatMessageValidator(buildPublicKeyList(0, 1))
        val message = messageFixtures[1]
        assertTrue(validator.validate(message))
    }

    /**
     * Tests that a unicode (korean) message is correctly validated.
     */
    @Test
    fun testUnicodeKoreanValidMessageOneKey() {
        validator = ChatMessageValidator(buildPublicKeyList(0, 1))
        val message = messageFixtures[2]
        assertTrue(validator.validate(message))
    }

    /**
     * Tests that when the signature is not a valid base64 string, the validator
     * simply says the message is not valid.
     */
    @Test
    fun testInvalidBase64Signature() {
        validator = ChatMessageValidator(buildPublicKeyList(0, 1))
        val message = buildChatMessage("This is a message!", "This is not valid base64...")
        assertFalse(validator.validate(message))
    }

    /**
     * Tests that when the public key associated with the validator is not a valid
     * base64 string, the validator simply says the message is not valid.
     */
    @Test
    fun testInvalidBase64PublicKey() {
        val list = buildPublicKeyList(0, 1)
        list[0].key = "This is not valid base 64"
        validator = ChatMessageValidator(list)
        val message = messageFixtures.first()
        assertFalse(validator.validate(message))
    }

    /**
     * Tests that when the validator does not have any public keys associated to it,
     * no message is found valid.
     */
    @Test
    fun testNoPublicKeys() {
        validator = ChatMessageValidator(ArrayList())
        for (message in messageFixtures) {
            assertFalse(validator.validate(message))
        }
    }

    /**
     * Tests that a message is found valid regardless of the fact that there is one invalid
     * key in the list of public keys.
     */
    @Test
    fun testOneInvalidKey() {
        val list = listOf(
                ChatPublicKey("This is not a valid key"),
                publicKeyFixtures[0]
        )
        validator = ChatMessageValidator(list)
        val message = messageFixtures[0]

        assertTrue(validator.validate(message))
    }

}
