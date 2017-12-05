package de.tum.in.tumcampusapp.activities;

import android.database.Cursor;
import android.support.v4.view.ViewPager;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import de.tum.in.tumcampusapp.BuildConfig;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.adapters.KinoAdapter;
import de.tum.in.tumcampusapp.shadows.KinoManagerShadow;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, shadows={KinoManagerShadow.class})
@PrepareForTest(KinoActivity.class)
public class KinoActivityTest {
    private KinoActivity kinoActivity;

    @Before
    public void setUp() {
        KinoManagerShadow.returnedCursor = Mockito.mock(Cursor.class);
    }

    /**
     * Default usage - there are some movies
     * Expected output: default Kino activity layout
     */
    @Test
    public void mainComponentDisplayedTest() {
        Mockito.when(KinoManagerShadow.returnedCursor.getCount()).thenReturn(1);
        kinoActivity = Robolectric.buildActivity(KinoActivity.class).create().start().get();

        assertThat(kinoActivity.findViewById(R.id.drawer_layout).getVisibility()).isEqualTo(View.VISIBLE);
    }

    /**
     * There are no movies to display
     * Expected output: no movies layout displayed
     */
    @Test
    public void mainComponentNoMoviesDisplayedTest() {
        Mockito.when(KinoManagerShadow.returnedCursor.getCount()).thenReturn(0);
        kinoActivity = Robolectric.buildActivity(KinoActivity.class).create().start().get();

        assertThat(kinoActivity.findViewById(R.id.no_movies_layout).getVisibility()).isEqualTo(View.VISIBLE);
    }

    /**
     * There are movies available
     * Expected output: KinoAdapter is used for pager.
     */
    @Test
    public void kinoAdapterUsedTest() {
        Mockito.when(KinoManagerShadow.returnedCursor.getCount()).thenReturn(1);
        kinoActivity = Robolectric.buildActivity(KinoActivity.class).create().start().get();

        assertThat(((ViewPager)kinoActivity.findViewById(R.id.pager)).getAdapter().getClass()).isEqualTo(KinoAdapter.class);
    }
}
