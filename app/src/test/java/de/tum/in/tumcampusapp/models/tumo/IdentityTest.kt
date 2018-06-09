package de.tum.`in`.tumcampusapp.models.tumo

import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.TestApp
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineConst
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.simpleframework.xml.core.Persister

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApp::class)
class IdentityTest {

    @Test
    fun testParsingXML() {
        val res = Persister().read(TUMOnlineConst.IDENTITY.response, XML_RESPONSE)
        assert(res.ids.size == 1)
        val identity = res.ids[0]
        assert(identity.familienname == FAMILIENNAME_EXPECTED)
        assert(identity.vorname == VORNAME_EXPECTED)
        assert(identity.kennung == KENNUNG_EXPECTED)
        assert(identity.obfuscated_id == OBFUSCATED_ID_EXPECTED)
    }

    companion object {
        private const val KENNUNG_EXPECTED = "anId"
        private const val VORNAME_EXPECTED = "Max"
        private const val FAMILIENNAME_EXPECTED = "Mustermann"
        private const val OBFUSCATED_ID_EXPECTED = "anobfuscatedID"
        private const val XML_RESPONSE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "                                                                   <rowset>\n" +
                "                                                                   <row>\n" +
                "                                                                   <kennung>$KENNUNG_EXPECTED</kennung>\n" +
                "                                                                   <vorname>$VORNAME_EXPECTED</vorname>\n" +
                "                                                                   <familienname>$FAMILIENNAME_EXPECTED</familienname>\n" +
                "                                                                   <obfuscated_id>$OBFUSCATED_ID_EXPECTED</obfuscated_id>\n" +
                "                                                                   <obfuscated_ids>\n" +
                "                                                                   <studierende>asad</studierende>\n" +
                "                                                                   <bedienstete isnull=\"true\"></bedienstete>\n" +
                "                                                                   <extern isnull=\"true\"></extern>\n" +
                "                                                                   </obfuscated_ids>\n" +
                "                                                                   </row>\n" +
                "                                                                   </rowset>"
    }
}