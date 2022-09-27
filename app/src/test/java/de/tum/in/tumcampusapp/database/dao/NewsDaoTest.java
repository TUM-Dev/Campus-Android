package de.tum.in.tumcampusapp.database.dao;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import androidx.test.core.app.ApplicationProvider;
import de.tum.in.tumcampusapp.TestApp;
import de.tum.in.tumcampusapp.component.ui.news.NewsDao;
import de.tum.in.tumcampusapp.component.ui.news.model.News;
import de.tum.in.tumcampusapp.database.TcaDb;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
@RunWith(RobolectricTestRunner.class)
@Config(application = TestApp.class)
public class NewsDaoTest {
    private NewsDao dao;
    private int newsIdx;

    @Before
    public void setUp() {
        dao = TcaDb.Companion.getInstance(ApplicationProvider.getApplicationContext()).newsDao();
        newsIdx = 0;
    }

    @After
    public void tearDown() {
        dao.flush();
        TcaDb.Companion.getInstance(ApplicationProvider.getApplicationContext()).close();
    }

    private News createNewsItem(String source, DateTime date) {
        News news = new News(Integer.toString(newsIdx),
                Integer.toString(newsIdx),
                "dummy link",
                source,
                "dummy image",
                date,
                date,
                0);
        newsIdx++;
        return news;
    }

    /**
     * Test clean up for 3 month old news items (which are all)
     * Expected output: all items are cleared - empty database
     */
    @Test
    public void cleanUpOldTest() {
        DateTime now = DateTime.now();
        dao.insert(createNewsItem("123", now.minusMonths(3).minusDays(1)));
        dao.insert(createNewsItem("123", now.minusMonths(10)));
        dao.insert(createNewsItem("123", now.minusYears(1)));
        dao.insert(createNewsItem("123", now.minusYears(3)));

        // before testing, make sure all items are there
        assertThat(dao.getAll(new Integer[]{123}, 123)).hasSize(4);
        dao.cleanUp();
        assertThat(dao.getAll(new Integer[]{123}, 123)).hasSize(0);
    }

    /**
     * Test clean up for items that are still new
     * Expected output: all items remain
     */
    @Test
    public void cleanUpNothingTest() {
        DateTime now = DateTime.now();
        dao.insert(createNewsItem("123", now.minusMonths(2).minusDays(1)));
        dao.insert(createNewsItem("123", now));
        dao.insert(createNewsItem("123", now.plusDays(1)));
        dao.insert(createNewsItem("123", now.plusYears(1)));

        // before testing, make sure all items are there
        assertThat(dao.getAll(new Integer[]{123}, 123)).hasSize(4);
        dao.cleanUp();
        assertThat(dao.getAll(new Integer[]{123}, 123)).hasSize(4);
    }

    /**
     * Test clean up for various date items
     * Expected output: some items are cleared, some remain
     */
    @Test
    public void cleanUpMixedTest() {
        DateTime now = DateTime.now();
        dao.insert(createNewsItem("123", now.minusMonths(5)));
        dao.insert(createNewsItem("123", now.minusDays(100)));
        dao.insert(createNewsItem("123", now.minusMonths(1)));
        dao.insert(createNewsItem("123", now));

        // before testing, make sure all items are there
        assertThat(dao.getAll(new Integer[]{123}, 123)).hasSize(4);
        dao.cleanUp();
        assertThat(dao.getAll(new Integer[]{123}, 123)).hasSize(2);
    }

    /**
     * Several items with different sources - get for single source
     * Expected output: several items are retrieved
     */
    @Test
    public void getAllSingleSourceTest() {
        DateTime now = DateTime.now();
        dao.insert(createNewsItem("123", now.minusMonths(5)));
        dao.insert(createNewsItem("124", now.minusDays(100)));
        dao.insert(createNewsItem("125", now.minusMonths(1)));
        dao.insert(createNewsItem("123", now));

        assertThat(dao.getAll(new Integer[]{123}, 999)).hasSize(2);
    }

    /**
     * Several items with different sources, including selected newspread
     * Expected output: several items are retrieved
     */
    @Test
    public void getAllSelectedSourceTest() {
        DateTime now = DateTime.now();
        dao.insert(createNewsItem("999", now.minusMonths(5)));
        dao.insert(createNewsItem("999", now.minusDays(100)));
        dao.insert(createNewsItem("125", now.minusMonths(1)));
        dao.insert(createNewsItem("999", now));

        assertThat(dao.getAll(new Integer[]{123, 999}, 999)).hasSize(3);
    }

    /**
     * Several items with multiple sources - get for single source
     * Expected output: several items are retrieved from different sources
     */
    @Test
    public void getAllMultiSourceTest() {
        DateTime now = DateTime.now();
        dao.insert(createNewsItem("123", now.minusMonths(5)));
        dao.insert(createNewsItem("124", now.minusDays(100)));
        dao.insert(createNewsItem("125", now.minusMonths(1)));
        dao.insert(createNewsItem("123", now));

        assertThat(dao.getAll(new Integer[]{123, 124}, 999)).hasSize(3);
    }

    /**
     * News items with dates in future
     * Expected output: All items are retrieved
     */
    @Test
    public void getNewerAllTest() {
        DateTime now = DateTime.now();
        dao.insert(createNewsItem("123", now.plusDays(1)));
        dao.insert(createNewsItem("123", now.plusMonths(1)));
        dao.insert(createNewsItem("123", now.plusYears(1)));
        dao.insert(createNewsItem("123", now.plusHours(100)));

        assertThat(dao.getNewer(123)).hasSize(4);
    }

    /**
     * Some of news items have dates in future
     * Expected output: Some items are retrieved
     */
    @Test
    public void getNewerSomeTest() {
        DateTime now = DateTime.now();
        dao.insert(createNewsItem("123", now.minusDays(1)));
        dao.insert(createNewsItem("123", now.plusMonths(1)));
        dao.insert(createNewsItem("123", now.plusYears(1)));
        dao.insert(createNewsItem("123", now.minusHours(1)));

        assertThat(dao.getNewer(123)).hasSize(2);
    }

    /**
     * All dates are in the past for news items
     * Expected output: No items retrieved
     */
    @Test
    public void getNewerNoneTest() {
        DateTime now = DateTime.now();
        dao.insert(createNewsItem("123", now.minusDays(1)));
        dao.insert(createNewsItem("123", now.minusMonths(1)));
        dao.insert(createNewsItem("123", now.minusYears(1)));
        dao.insert(createNewsItem("123", now.minusHours(1)));

        assertThat(dao.getNewer(123)).hasSize(0);
    }

    /**
     * Several news items and "biggest" id one is retrieved
     * Expected output: item with biggest id is retrieved
     */
    @Test
    public void getLastTest() {
        DateTime now = DateTime.now();
        dao.insert(createNewsItem("123", now.minusDays(1)));
        dao.insert(createNewsItem("123", now.minusMonths(1)));
        dao.insert(createNewsItem("123", now.minusYears(1)));
        dao.insert(createNewsItem("123", now.minusHours(1)));

        News last = dao.getLast();
        assertThat(last.getId()).isEqualTo("3");
    }

    /**
     * News items with different sources and all match
     * Expected output: All items are retrieved
     */
    @Test
    public void getBySourcesAllTest() {
        DateTime now = DateTime.now();
        dao.insert(createNewsItem("123", now.plusDays(1)));
        dao.insert(createNewsItem("124", now.plusMonths(1)));
        dao.insert(createNewsItem("125", now.plusYears(1)));
        dao.insert(createNewsItem("126", now.plusHours(1)));

        assertThat(dao.getBySources(new Integer[]{123, 124, 125, 126})).hasSize(4);
    }

    /**
     * News items with different sources and some match
     * Expected output: Some items are retrieved
     */
    @Test
    public void getBySourcesSomeTest() {
        DateTime now = DateTime.now();
        dao.insert(createNewsItem("123", now.plusDays(1)));
        dao.insert(createNewsItem("124", now.plusMonths(1)));
        dao.insert(createNewsItem("125", now.plusYears(1)));
        dao.insert(createNewsItem("126", now.plusHours(1)));

        assertThat(dao.getBySources(new Integer[]{123, 124})).hasSize(2);
    }

    /**
     * News items with different sources and some match
     * Expected output: No items retrieved
     */
    @Test
    public void getBySourcesNoneTest() {
        DateTime now = DateTime.now();
        dao.insert(createNewsItem("123", now.plusDays(1)));
        dao.insert(createNewsItem("124", now.plusMonths(1)));
        dao.insert(createNewsItem("125", now.plusYears(1)));
        dao.insert(createNewsItem("126", now.plusHours(1)));

        assertThat(dao.getBySources(new Integer[]{127, 128})).hasSize(0);
    }

    /**
     * Closest to today items retrieved single per source.
     * Expected output: All items are retrieved
     */
    @Test
    public void getBySourcesLatestTest() {
        DateTime now = DateTime.now();
        dao.insert(createNewsItem("123", now.minusDays(1)));
        dao.insert(createNewsItem("124", now.minusMonths(1)));
        dao.insert(createNewsItem("125", now.minusYears(1)));
        dao.insert(createNewsItem("126", now.minusHours(1)));

        List<News> news = dao.getBySources(new Integer[]{123, 124, 125, 126});
        assertThat(news).hasSize(4);
    }

    /**
     * There are several items per source
     * Expected output: Some items are retrieved
     */
    @Test
    public void getBySourcesLatestSomeTest() {
        DateTime now = DateTime.now();
        dao.insert(createNewsItem("123", now.minusDays(1)));
        dao.insert(createNewsItem("123", now.minusMonths(1)));
        dao.insert(createNewsItem("124", now.minusYears(1)));
        dao.insert(createNewsItem("124", now.minusHours(100)));

        List<News> news = dao.getBySourcesLatest(new Integer[]{123, 124, 125, 126});
        assertThat(news).hasSize(2);
        assertThat(news.get(0).getId()).isEqualTo("3");
        assertThat(news.get(1).getId()).isEqualTo("0");
    }

    /**
     * All items are in future
     * Expected output: No items retrieved
     */
    @Test
    public void getBySourcesLatestNoneTest() {
        DateTime now = DateTime.now();
        dao.insert(createNewsItem("123", now.plusDays(1)));
        dao.insert(createNewsItem("124", now.plusMonths(1)));
        dao.insert(createNewsItem("125", now.plusYears(1)));
        dao.insert(createNewsItem("126", now.plusHours(30)));

        // before testing, make sure all items are there
        assertThat(dao.getBySourcesLatest(new Integer[]{123, 124, 125, 126, 127})).hasSize(0);
    }

    /**
     * Special treatment for Kino item which should be in future
     * Expected output: Single item retrieved
     */
    @Test
    public void getBySourcesLatestKinoTest() {
        DateTime now = DateTime.now();
        // NOTE: Kino source number is hardcoded 2 (through server's backend)
        dao.insert(createNewsItem("2", now.plusDays(1)));
        dao.insert(createNewsItem("2", now.plusMonths(1)));
        dao.insert(createNewsItem("125", now.plusYears(1)));
        dao.insert(createNewsItem("126", now.plusHours(1)));

        // before testing, make sure all items are there
        assertThat(dao.getBySourcesLatest(new Integer[]{127, 2})).hasSize(1);
    }

    /**
     * Mixed sample - multiple Kino items, some items in future, some in past
     * Expected output: severla items retrieved
     */
    @Test
    public void getBySourcesLatestMixedTest() {
        DateTime now = DateTime.now();
        // NOTE: Kino source number is hardcoded 2 (through server's backend)
        dao.insert(createNewsItem("2", now.plusDays(1))); //has to be picked
        dao.insert(createNewsItem("2", now.plusMonths(1)));
        dao.insert(createNewsItem("125", now.minusMonths(1))); //has to be picked
        dao.insert(createNewsItem("126", now.plusHours(27)));

        // before testing, make sure all items are there
        assertThat(dao.getBySourcesLatest(new Integer[]{125, 126, 2})).hasSize(2);
    }
}
