package de.tum.in.tumcampusapp.database.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import de.tum.in.tumcampusapp.BuildConfig;
import de.tum.in.tumcampusapp.TestApp;
import de.tum.in.tumcampusapp.component.ui.news.NewsSourcesDao;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsSources;
import de.tum.in.tumcampusapp.database.TcaDb;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, application = TestApp.class)
public class NewsSourcesDaoTest {
    private NewsSourcesDao dao;

    @Before
    public void setUp() {
        dao = TcaDb.getInstance(RuntimeEnvironment.application).newsSourcesDao();
    }

    @After
    public void tearDown() {
        dao.flush();
        TcaDb.getInstance(RuntimeEnvironment.application).close();
    }

    /**
     * Test that all news sources are returned
     * Expected output: all items are returned
     */
    @Test
    public void getNewsSourcesAllTest() {
        dao.insert(new NewsSources(0, "0", ""));
        dao.insert(new NewsSources(1, "1", ""));
        dao.insert(new NewsSources(14, "2", ""));
        dao.insert(new NewsSources(15, "3", ""));

        assertThat(dao.getNewsSources("1")).hasSize(4);
    }

    /**
     * Test that only in "allowed" range sources are returned
     * Expected output: some items are returned
     */
    @Test
    public void getNewsSourcesSomeTest() {
        dao.insert(new NewsSources(0, "0", ""));
        dao.insert(new NewsSources(9, "1", "")); // should be excluded
        dao.insert(new NewsSources(10, "2", "")); // should be excluded
        dao.insert(new NewsSources(15, "3", ""));

        assertThat(dao.getNewsSources("1")).hasSize(2);
    }

    /**
     * Test that all that are in excluded range are not returned
     * Expected output: no items returned
     */
    @Test
    public void getNewsSourcesNoneTest() {
        dao.insert(new NewsSources(8, "0", ""));
        dao.insert(new NewsSources(9, "1", ""));
        dao.insert(new NewsSources(10, "2", ""));
        dao.insert(new NewsSources(11, "3", ""));

        assertThat(dao.getNewsSources("1")).hasSize(0);
    }

    /**
     * Test that selected source from excluded range is returned
     * Expected output: single item
     */
    @Test
    public void getNewsSourcesSelectedSourceTest() {
        dao.insert(new NewsSources(8, "0", ""));
        dao.insert(new NewsSources(9, "1", ""));
        dao.insert(new NewsSources(10, "2", ""));
        dao.insert(new NewsSources(11, "3", ""));

        assertThat(dao.getNewsSources("9")).hasSize(1);
    }

    /**
     * Test that specific id item is returned
     * Expected output: actual item is returned
     */
    @Test
    public void getNewsSourceTest() {
        dao.insert(new NewsSources(8, "8", "8"));
        dao.insert(new NewsSources(9, "9", "9"));
        dao.insert(new NewsSources(10, "10", "10"));
        dao.insert(new NewsSources(11, "11", "11"));

        NewsSources item = dao.getNewsSource(9);
        assertThat(item.getId()).isEqualTo(9);
        assertThat(item.getTitle()).isEqualTo("9");
        assertThat(item.getIcon()).isEqualTo("9");
    }
}
