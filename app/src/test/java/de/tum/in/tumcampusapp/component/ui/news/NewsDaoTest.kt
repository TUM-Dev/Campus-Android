package de.tum.`in`.tumcampusapp.component.ui.news

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import de.tum.`in`.tumcampusapp.component.ui.news.model.News
import de.tum.`in`.tumcampusapp.database.TcaDb
import net.danlew.android.joda.JodaTimeAndroid
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewsDaoTest {

    private lateinit var database: TcaDb

    private val dao: NewsDao by lazy {
        database.newsDao()
    }

    private var index: Int = 0

    @Before
    fun initDatabase() {
        val context = InstrumentationRegistry.getContext()
        database = Room.inMemoryDatabaseBuilder(context, TcaDb::class.java)
                .allowMainThreadQueries()
                .build()
        index = 0
        JodaTimeAndroid.init(context)
    }

    @After
    fun closeDb() {
        database.close()
    }

    private fun createNewsItem(source: String, date: DateTime): News {
        val news = News(index.toString(), "title $index", "dummy link",
                        source, "dummy image", date, date, 0)
        index++
        return news
    }

    /**
     * Test clean up for 3 month old news items (which are all)
     * Expected output: all items are cleared - empty database
     */
    @Test
    fun cleanUpOldTest() {
        val now = DateTime.now()
        dao.insert(createNewsItem("123", now.minusMonths(3).minusDays(1)))
        dao.insert(createNewsItem("123", now.minusMonths(10)))
        dao.insert(createNewsItem("123", now.minusYears(1)))
        dao.insert(createNewsItem("123", now.minusYears(3)))

        // before testing, make sure all items are there
        assertThat(dao.getAll(arrayOf(123), 123)).hasSize(4)
        dao.cleanUp()
        assertThat(dao.getAll(arrayOf(123), 123)).hasSize(0)
    }

    /**
     * Test clean up for items that are still new
     * Expected output: all items remain
     */
    @Test
    fun cleanUpNothingTest() {
        val now = DateTime.now()
        dao.insert(createNewsItem("123", now.minusMonths(2).minusDays(1)))
        dao.insert(createNewsItem("123", now))
        dao.insert(createNewsItem("123", now.plusDays(1)))
        dao.insert(createNewsItem("123", now.plusYears(1)))

        // before testing, make sure all items are there
        assertThat(dao.getAll(arrayOf(123), 123)).hasSize(4)
        dao.cleanUp()
        assertThat(dao.getAll(arrayOf(123), 123)).hasSize(4)
    }

    /**
     * Test clean up for various date items
     * Expected output: some items are cleared, some remain
     */
    @Test
    fun cleanUpMixedTest() {
        val now = DateTime.now()
        dao.insert(createNewsItem("123", now.minusMonths(5)))
        dao.insert(createNewsItem("123", now.minusDays(100)))
        dao.insert(createNewsItem("123", now.minusMonths(1)))
        dao.insert(createNewsItem("123", now))

        // before testing, make sure all items are there
        assertThat(dao.getAll(arrayOf(123), 123)).hasSize(4)
        dao.cleanUp()
        assertThat(dao.getAll(arrayOf(123), 123)).hasSize(2)
    }

    /**
     * Several items with different sources - get for single source
     * Expected output: several items are retrieved
     */
    @Test
    fun getAllSingleSourceTest() {
        val now = DateTime.now()
        dao.insert(createNewsItem("123", now.minusMonths(5)))
        dao.insert(createNewsItem("124", now.minusDays(100)))
        dao.insert(createNewsItem("125", now.minusMonths(1)))
        dao.insert(createNewsItem("123", now))

        assertThat(dao.getAll(arrayOf(123), 999)).hasSize(2)
    }

    /**
     * Several items with different sources, including selected newspread
     * Expected output: several items are retrieved
     */
    @Test
    fun getAllSelectedSourceTest() {
        val now = DateTime.now()
        dao.insert(createNewsItem("999", now.minusMonths(5)))
        dao.insert(createNewsItem("999", now.minusDays(100)))
        dao.insert(createNewsItem("125", now.minusMonths(1)))
        dao.insert(createNewsItem("999", now))

        assertThat(dao.getAll(arrayOf(123, 999), 999)).hasSize(3)
    }

    /**
     * Several items with multiple sources - get for single source
     * Expected output: several items are retrieved from different sources
     */
    @Test
    fun getAllMultiSourceTest() {
        val now = DateTime.now()
        dao.insert(createNewsItem("123", now.minusMonths(5)))
        dao.insert(createNewsItem("124", now.minusDays(100)))
        dao.insert(createNewsItem("125", now.minusMonths(1)))
        dao.insert(createNewsItem("123", now))

        assertThat(dao.getAll(arrayOf(123, 124), 999)).hasSize(3)
    }

    /**
     * News items with dates in future
     * Expected output: All items are retrieved
     */
    @Test
    fun getNewerAllTest() {
        val now = DateTime.now()
        dao.insert(createNewsItem("123", now.plusDays(1)))
        dao.insert(createNewsItem("123", now.plusMonths(1)))
        dao.insert(createNewsItem("123", now.plusYears(1)))
        dao.insert(createNewsItem("123", now.plusHours(100)))

        assertThat(dao.getNewer(123)).hasSize(4)
    }

    /**
     * Some of news items have dates in future
     * Expected output: Some items are retrieved
     */
    @Test
    fun getNewerSomeTest() {
        val now = DateTime.now()
        dao.insert(createNewsItem("123", now.minusDays(1)))
        dao.insert(createNewsItem("123", now.plusMonths(1)))
        dao.insert(createNewsItem("123", now.plusYears(1)))
        dao.insert(createNewsItem("123", now.minusHours(1)))

        assertThat(dao.getNewer(123)).hasSize(2)
    }

    /**
     * All dates are in the past for news items
     * Expected output: No items retrieved
     */
    @Test
    fun getNewerNoneTest() {
        val now = DateTime.now()
        dao.insert(createNewsItem("123", now.minusDays(1)))
        dao.insert(createNewsItem("123", now.minusMonths(1)))
        dao.insert(createNewsItem("123", now.minusYears(1)))
        dao.insert(createNewsItem("123", now.minusHours(1)))

        assertThat(dao.getNewer(123)).hasSize(0)
    }

    /**
     * Several news items and "biggest" id one is retrieved
     * Expected output: item with biggest id is retrieved
     */
    @Test
    fun getLastTest() {
        val now = DateTime.now()
        dao.insert(createNewsItem("123", now.minusDays(1)))
        dao.insert(createNewsItem("123", now.minusMonths(1)))
        dao.insert(createNewsItem("123", now.minusYears(1)))
        dao.insert(createNewsItem("123", now.minusHours(1)))

        val last = dao.last
        assertThat(last?.id).isEqualTo("3")
    }

    /**
     * News items with different sources and all match
     * Expected output: All items are retrieved
     */
    @Test
    fun getBySourcesAllTest() {
        val now = DateTime.now()
        dao.insert(createNewsItem("123", now.plusDays(1)))
        dao.insert(createNewsItem("124", now.plusMonths(1)))
        dao.insert(createNewsItem("125", now.plusYears(1)))
        dao.insert(createNewsItem("126", now.plusHours(1)))

        assertThat(dao.getBySources(arrayOf(123, 124, 125, 126))).hasSize(4)
    }

    /**
     * News items with different sources and some match
     * Expected output: Some items are retrieved
     */
    @Test
    fun getBySourcesSomeTest() {
        val now = DateTime.now()
        dao.insert(createNewsItem("123", now.plusDays(1)))
        dao.insert(createNewsItem("124", now.plusMonths(1)))
        dao.insert(createNewsItem("125", now.plusYears(1)))
        dao.insert(createNewsItem("126", now.plusHours(1)))

        assertThat(dao.getBySources(arrayOf(123, 124))).hasSize(2)
    }

    /**
     * News items with different sources and some match
     * Expected output: No items retrieved
     */
    @Test
    fun getBySourcesNoneTest() {
        val now = DateTime.now()
        dao.insert(createNewsItem("123", now.plusDays(1)))
        dao.insert(createNewsItem("124", now.plusMonths(1)))
        dao.insert(createNewsItem("125", now.plusYears(1)))
        dao.insert(createNewsItem("126", now.plusHours(1)))

        assertThat(dao.getBySources(arrayOf(127, 128))).hasSize(0)
    }

    /**
     * Closest to today items retrieved single per source.
     * Expected output: All items are retrieved
     */
    @Test
    fun getBySourcesLatestTest() {
        val now = DateTime.now()
        dao.insert(createNewsItem("123", now.minusDays(1)))
        dao.insert(createNewsItem("124", now.minusMonths(1)))
        dao.insert(createNewsItem("125", now.minusYears(1)))
        dao.insert(createNewsItem("126", now.minusHours(1)))

        val news = dao.getBySources(arrayOf(123, 124, 125, 126))
        assertThat(news).hasSize(4)
    }

    /**
     * There are several items per source
     * Expected output: Some items are retrieved
     */
    @Test
    fun getBySourcesLatestSomeTest() {
        val now = DateTime.now()
        dao.insert(createNewsItem("123", now.minusDays(1)))
        dao.insert(createNewsItem("123", now.minusMonths(1)))
        dao.insert(createNewsItem("124", now.minusYears(1)))
        dao.insert(createNewsItem("124", now.minusHours(100)))

        val news = dao.getBySourcesLatest(arrayOf(123, 124, 125, 126))
        assertThat(news).hasSize(2)
        assertThat(news[0].id).isEqualTo("3")
        assertThat(news[1].id).isEqualTo("0")
    }

    /**
     * All items are in future
     * Expected output: No items retrieved
     */
    @Test
    fun getBySourcesLatestNoneTest() {
        val now = DateTime.now()
        dao.insert(createNewsItem("123", now.plusDays(1)))
        dao.insert(createNewsItem("124", now.plusMonths(1)))
        dao.insert(createNewsItem("125", now.plusYears(1)))
        dao.insert(createNewsItem("126", now.plusHours(30)))

        // before testing, make sure all items are there
        assertThat(dao.getBySourcesLatest(arrayOf(123, 124, 125, 126, 127))).hasSize(0)
    }

    /**
     * Special treatment for Kino item which should be in future
     * Expected output: Single item retrieved
     */
    @Test
    fun getBySourcesLatestKinoTest() {
        val now = DateTime.now()
        // NOTE: Kino source number is hardcoded 2 (through server's backend)
        dao.insert(createNewsItem("2", now.plusDays(1)))
        dao.insert(createNewsItem("2", now.plusMonths(1)))
        dao.insert(createNewsItem("125", now.plusYears(1)))
        dao.insert(createNewsItem("126", now.plusHours(1)))

        // before testing, make sure all items are there
        assertThat(dao.getBySourcesLatest(arrayOf(127, 2))).hasSize(1)
    }

    /**
     * Mixed sample - multiple Kino items, some items in future, some in past
     * Expected output: severla items retrieved
     */
    @Test
    fun getBySourcesLatestMixedTest() {
        val now = DateTime.now()
        // NOTE: Kino source number is hardcoded 2 (through server's backend)
        dao.insert(createNewsItem("2", now.plusDays(1))) //has to be picked
        dao.insert(createNewsItem("2", now.plusMonths(1)))
        dao.insert(createNewsItem("125", now.minusMonths(1))) //has to be picked
        dao.insert(createNewsItem("126", now.plusHours(27)))

        // before testing, make sure all items are there
        assertThat(dao.getBySourcesLatest(arrayOf(125, 126, 2))).hasSize(2)
    }
}
