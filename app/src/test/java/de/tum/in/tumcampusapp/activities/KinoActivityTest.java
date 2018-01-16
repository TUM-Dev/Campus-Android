package de.tum.in.tumcampusapp.activities;

import android.support.v4.view.ViewPager;
import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import de.tum.in.tumcampusapp.BuildConfig;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.adapters.KinoAdapter;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dao.KinoDao;
import de.tum.in.tumcampusapp.models.tumcabe.Kino;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class KinoActivityTest {
    private KinoActivity kinoActivity;
    private KinoDao dao;

    @Before
    public void setUp() {
        dao = TcaDb.getInstance(RuntimeEnvironment.application).kinoDao();
        dao.flush();
    }

    @After
    public void tearDown() {
        TcaDb.getInstance(RuntimeEnvironment.application).close();
    }

    /**
     * Default usage - there are some movies
     * Expected output: default Kino activity layout
     */
    @Test
    public void mainComponentDisplayedTest() {
        dao.insert(new Kino());
        kinoActivity = Robolectric.buildActivity(KinoActivity.class).create().start().get();

        assertThat(kinoActivity.findViewById(R.id.drawer_layout).getVisibility()).isEqualTo(View.VISIBLE);
    }

    /**
     * There are no movies to display
     * Expected output: no movies layout displayed
     */
    @Test
    public void mainComponentNoMoviesDisplayedTest() {
        kinoActivity = Robolectric.buildActivity(KinoActivity.class).create().start().get();

        assertThat(kinoActivity.findViewById(R.id.no_movies_layout).getVisibility()).isEqualTo(View.VISIBLE);
    }

    /**
     * There are movies available
     * Expected output: KinoAdapter is used for pager.
     */
    @Test
    public void kinoAdapterUsedTest() {
        dao.insert(new Kino());
        kinoActivity = Robolectric.buildActivity(KinoActivity.class).create().start().get();

        assertThat(((ViewPager)kinoActivity.findViewById(R.id.pager)).getAdapter().getClass()).isEqualTo(KinoAdapter.class);
    }
}
