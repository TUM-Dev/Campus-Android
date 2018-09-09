package de.tum.`in`.tumcampusapp.database.dao

import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.TestApp
import de.tum.`in`.tumcampusapp.component.ui.news.NewsSourcesDao
import de.tum.`in`.tumcampusapp.component.ui.news.model.NewsSources
import de.tum.`in`.tumcampusapp.database.TcaDb
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Ignore
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApp::class)
class NewsSourcesDaoTest {
    private var dao: NewsSourcesDao? = null

    @Before
    fun setUp() {
        dao = TcaDb.getInstance(RuntimeEnvironment.application).newsSourcesDao()
    }

    @After
    fun tearDown() {
        dao!!.flush()
        TcaDb.getInstance(RuntimeEnvironment.application).close()
    }

    /**
     * Test that all news sources are returned
     * Expected output: all items are returned
     */
    @Test
    fun getNewsSourcesAllTest() {
        dao!!.insert(NewsSources(0, "0", ""))
        dao!!.insert(NewsSources(1, "1", ""))
        dao!!.insert(NewsSources(14, "2", ""))
        dao!!.insert(NewsSources(15, "3", ""))

        assertThat(dao!!.getNewsSources("1")).hasSize(4)
    }

    /**
     * Test that only in "allowed" range sources are returned
     * Expected output: some items are returned
     */
    @Test
    fun getNewsSourcesSomeTest() {
        dao!!.insert(NewsSources(0, "0", ""))
        dao!!.insert(NewsSources(9, "1", "")) // should be excluded
        dao!!.insert(NewsSources(10, "2", "")) // should be excluded
        dao!!.insert(NewsSources(15, "3", ""))

        assertThat(dao!!.getNewsSources("1")).hasSize(2)
    }

    /**
     * Test that all that are in excluded range are not returned
     * Expected output: no items returned
     */
    @Test
    fun getNewsSourcesNoneTest() {
        dao!!.insert(NewsSources(8, "0", ""))
        dao!!.insert(NewsSources(9, "1", ""))
        dao!!.insert(NewsSources(10, "2", ""))
        dao!!.insert(NewsSources(11, "3", ""))

        assertThat(dao!!.getNewsSources("1")).hasSize(0)
    }

    /**
     * Test that selected source from excluded range is returned
     * Expected output: single item
     */
    @Test
    fun getNewsSourcesSelectedSourceTest() {
        dao!!.insert(NewsSources(8, "0", ""))
        dao!!.insert(NewsSources(9, "1", ""))
        dao!!.insert(NewsSources(10, "2", ""))
        dao!!.insert(NewsSources(11, "3", ""))

        assertThat(dao!!.getNewsSources("9")).hasSize(1)
    }

    /**
     * Test that specific id item is returned
     * Expected output: actual item is returned
     */
    @Test
    fun getNewsSourceTest() {
        dao!!.insert(NewsSources(8, "8", "8"))
        dao!!.insert(NewsSources(9, "9", "9"))
        dao!!.insert(NewsSources(10, "10", "10"))
        dao!!.insert(NewsSources(11, "11", "11"))

        val (id, title, icon) = dao!!.getNewsSource(9)
        assertThat(id).isEqualTo(9)
        assertThat(title).isEqualTo("9")
        assertThat(icon).isEqualTo("9")
    }
}
