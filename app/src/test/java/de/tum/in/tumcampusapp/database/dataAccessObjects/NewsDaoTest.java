package de.tum.in.tumcampusapp.database.dataAccessObjects;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Date;
import java.util.List;

import de.tum.in.tumcampusapp.BuildConfig;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.models.tumcabe.News;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class NewsDaoTest {
    private NewsDao dao;
    private int newsIdx;

    @Before
    public void setUp() {
        dao = TcaDb.getInstance(RuntimeEnvironment.application).newsDao();
        newsIdx = 0;
    }

    @After
    public void tearDown() {
        dao.flush();
        TcaDb.getInstance(RuntimeEnvironment.application).close();
    }

    private News createNewsItem(String source, Date date) {
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
        dao.insert(createNewsItem("123", DateTime.now().minusMonths(3).minusDays(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().minusMonths(10).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().minusYears(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().minusYears(3).toDate()));

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
        dao.insert(createNewsItem("123", DateTime.now().minusMonths(2).minusDays(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().toDate()));
        dao.insert(createNewsItem("123", DateTime.now().plusDays(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().plusYears(1).toDate()));

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
        dao.insert(createNewsItem("123", DateTime.now().minusMonths(5).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().minusDays(100).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().minusMonths(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().toDate()));

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
        dao.insert(createNewsItem("123", DateTime.now().minusMonths(5).toDate()));
        dao.insert(createNewsItem("124", DateTime.now().minusDays(100).toDate()));
        dao.insert(createNewsItem("125", DateTime.now().minusMonths(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().toDate()));

        assertThat(dao.getAll(new Integer[]{123}, 999)).hasSize(2);
    }

    /**
     * Several items with different sources, including selected newspread
     * Expected output: several items are retrieved
     */
    @Test
    public void getAllSelectedSourceTest() {
        dao.insert(createNewsItem("999", DateTime.now().minusMonths(5).toDate()));
        dao.insert(createNewsItem("999", DateTime.now().minusDays(100).toDate()));
        dao.insert(createNewsItem("125", DateTime.now().minusMonths(1).toDate()));
        dao.insert(createNewsItem("999", DateTime.now().toDate()));

        assertThat(dao.getAll(new Integer[]{123, 999}, 999)).hasSize(3);
    }

    /**
     * Several items with multiple sources - get for single source
     * Expected output: several items are retrieved from different sources
     */
    @Test
    public void getAllMultiSourceTest() {
        dao.insert(createNewsItem("123", DateTime.now().minusMonths(5).toDate()));
        dao.insert(createNewsItem("124", DateTime.now().minusDays(100).toDate()));
        dao.insert(createNewsItem("125", DateTime.now().minusMonths(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().toDate()));

        assertThat(dao.getAll(new Integer[]{123, 124}, 999)).hasSize(3);
    }

    /**
     * News items with dates in future
     * Expected output: All items are retrieved
     */
    @Test
    public void getNewerAllTest() {
        dao.insert(createNewsItem("123", DateTime.now().plusDays(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().plusMonths(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().plusYears(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().plusHours(100).toDate()));

        assertThat(dao.getNewer(123)).hasSize(4);
    }

    /**
     * Some of news items have dates in future
     * Expected output: Some items are retrieved
     */
    @Test
    public void getNewerSomeTest() {
        dao.insert(createNewsItem("123", DateTime.now().minusDays(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().plusMonths(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().plusYears(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().minusHours(1).toDate()));

        assertThat(dao.getNewer(123)).hasSize(2);
    }

    /**
     * All dates are in the past for news items
     * Expected output: No items retrieved
     */
    @Test
    public void getNewerNoneTest() {
        dao.insert(createNewsItem("123", DateTime.now().minusDays(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().minusMonths(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().minusYears(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().minusHours(1).toDate()));

        assertThat(dao.getNewer(123)).hasSize(0);
    }

    /**
     * Several news items and "biggest" id one is retrieved
     * Expected output: item with biggest id is retrieved
     */
    @Test
    public void getLastTest() {
        dao.insert(createNewsItem("123", DateTime.now().minusDays(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().minusMonths(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().minusYears(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().minusHours(1).toDate()));

        News last = dao.getLast();
        assertThat(last.getId()).isEqualTo("3");
    }

    /**
     * News items with different sources and all match
     * Expected output: All items are retrieved
     */
    @Test
    public void getBySourcesAllTest() {
        dao.insert(createNewsItem("123", DateTime.now().plusDays(1).toDate()));
        dao.insert(createNewsItem("124", DateTime.now().plusMonths(1).toDate()));
        dao.insert(createNewsItem("125", DateTime.now().plusYears(1).toDate()));
        dao.insert(createNewsItem("126", DateTime.now().plusHours(1).toDate()));

        assertThat(dao.getBySources(new Integer[]{123, 124, 125, 126})).hasSize(4);
    }

    /**
     * News items with different sources and some match
     * Expected output: Some items are retrieved
     */
    @Test
    public void getBySourcesSomeTest() {
        dao.insert(createNewsItem("123", DateTime.now().plusDays(1).toDate()));
        dao.insert(createNewsItem("124", DateTime.now().plusMonths(1).toDate()));
        dao.insert(createNewsItem("125", DateTime.now().plusYears(1).toDate()));
        dao.insert(createNewsItem("126", DateTime.now().plusHours(1).toDate()));

        assertThat(dao.getBySources(new Integer[]{123, 124})).hasSize(2);
    }

    /**
     * News items with different sources and some match
     * Expected output: No items retrieved
     */
    @Test
    public void getBySourcesNoneTest() {
        dao.insert(createNewsItem("123", DateTime.now().plusDays(1).toDate()));
        dao.insert(createNewsItem("124", DateTime.now().plusMonths(1).toDate()));
        dao.insert(createNewsItem("125", DateTime.now().plusYears(1).toDate()));
        dao.insert(createNewsItem("126", DateTime.now().plusHours(1).toDate()));

        assertThat(dao.getBySources(new Integer[]{127, 128})).hasSize(0);
    }

    /**
     * Closest to today items retrieved single per source.
     * Expected output: All items are retrieved
     */
    @Test
    public void getBySourcesLatestTest() {
        dao.insert(createNewsItem("123", DateTime.now().minusDays(1).toDate()));
        dao.insert(createNewsItem("124", DateTime.now().minusMonths(1).toDate()));
        dao.insert(createNewsItem("125", DateTime.now().minusYears(1).toDate()));
        dao.insert(createNewsItem("126", DateTime.now().minusHours(1).toDate()));

        List<News> news = dao.getBySources(new Integer[]{123, 124, 125, 126});
        assertThat(news).hasSize(4);
    }

    /**
     * There are several items per source
     * Expected output: Some items are retrieved
     */
    @Test
    public void getBySourcesLatestSomeTest() {
        dao.insert(createNewsItem("123", DateTime.now().minusDays(1).toDate()));
        dao.insert(createNewsItem("123", DateTime.now().minusMonths(1).toDate()));
        dao.insert(createNewsItem("124", DateTime.now().minusYears(1).toDate()));
        dao.insert(createNewsItem("124", DateTime.now().minusHours(100).toDate()));

        List<News> news = dao.getBySources(new Integer[]{123, 124, 125, 126});
        assertThat(news).hasSize(2);
        assertThat(news.get(0).getId()).isEqualTo("0");
        assertThat(news.get(1).getId()).isEqualTo("3");
    }

    /**
     * All items are in future
     * Expected output: No items retrieved
     */
    @Test
    public void getBySourcesLatestNoneTest() {
        dao.insert(createNewsItem("123", DateTime.now().plusDays(1).toDate()));
        dao.insert(createNewsItem("124", DateTime.now().plusMonths(1).toDate()));
        dao.insert(createNewsItem("125", DateTime.now().plusYears(1).toDate()));
        dao.insert(createNewsItem("126", DateTime.now().plusHours(1).toDate()));

        // before testing, make sure all items are there
        assertThat(dao.getBySources(new Integer[]{123, 124, 125, 126, 127})).hasSize(0);
    }

    /**
     * Special treatment for Kino item which should be in future
     * Expected output: Single item retrieved
     */
    @Test
    public void getBySourcesLatestKinoTest() {
        // NOTE: Kino source number is hardcoded 2 (through server's backend)
        dao.insert(createNewsItem("2", DateTime.now().plusDays(1).toDate()));
        dao.insert(createNewsItem("2", DateTime.now().plusMonths(1).toDate()));
        dao.insert(createNewsItem("125", DateTime.now().plusYears(1).toDate()));
        dao.insert(createNewsItem("126", DateTime.now().plusHours(1).toDate()));

        // before testing, make sure all items are there
        assertThat(dao.getBySources(new Integer[]{127, 2})).hasSize(1);
    }

    /**
     * Mixed sample - multiple Kino items, some items in future, some in past
     * Expected output: severla items retrieved
     */
    @Test
    public void getBySourcesLatestMixedTest() {
        // NOTE: Kino source number is hardcoded 2 (through server's backend)
        dao.insert(createNewsItem("2", DateTime.now().plusDays(1).toDate()));
        dao.insert(createNewsItem("2", DateTime.now().plusMonths(1).toDate()));
        dao.insert(createNewsItem("125", DateTime.now().minusMonths(1).toDate()));
        dao.insert(createNewsItem("126", DateTime.now().minusHours(1).toDate()));

        // before testing, make sure all items are there
        assertThat(dao.getBySources(new Integer[]{125, 126, 2})).hasSize(2);
    }
}
