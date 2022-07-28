package de.tum.`in`.tumcampusapp.models.tumo

import com.tickaroo.tikxml.TikXml
import de.tum.`in`.tumcampusapp.TestApp
import de.tum.`in`.tumcampusapp.component.tumui.person.model.IdentitySet
import okio.buffer
import okio.source
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApp::class)
class IdentityTest {

    @Test
    fun testParsingXML() {
        val tikXml = TikXml.Builder()
                .exceptionOnUnreadXml(false)
                .build()

        val inputStream = ByteArrayInputStream(XML_RESPONSE.toByteArray())
        val source = inputStream.source()
        val bufferedSource = source.buffer()

        val res = tikXml.read(bufferedSource, IdentitySet::class.java)
        assert(res.ids.size == 1)

        val identity = res.ids[0]
        assert(identity.lastName == FAMILIENNAME_EXPECTED)
        assert(identity.firstName == VORNAME_EXPECTED)
        assert(identity.id == KENNUNG_EXPECTED)
        assert(identity.obfuscated_id == OBFUSCATED_ID_EXPECTED)
    }

    companion object {
        private const val KENNUNG_EXPECTED = "anId"
        private const val VORNAME_EXPECTED = "Max"
        private const val FAMILIENNAME_EXPECTED = "Mustermann"
        private const val OBFUSCATED_ID_EXPECTED = "anobfuscatedID"
        private const val XML_RESPONSE = """<?xml version="1.0" encoding="utf-8"?>
                                                                   <rowset>
                                                                   <row>
                                                                   <kennung>$KENNUNG_EXPECTED</kennung>
                                                                   <vorname>$VORNAME_EXPECTED</vorname>
                                                                   <familienname>$FAMILIENNAME_EXPECTED</familienname>
                                                                   <obfuscated_id>$OBFUSCATED_ID_EXPECTED</obfuscated_id>
                                                                   <obfuscated_ids>
                                                                   <studierende>asad</studierende>
                                                                   <bedienstete isnull="true"></bedienstete>
                                                                   <extern isnull="true"></extern>
                                                                   </obfuscated_ids>
                                                                   </row>
                                                                   </rowset>"""
    }
}