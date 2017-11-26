package de.tum.in.tumcampusapp.activities;

import android.database.Cursor;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import de.tum.in.tumcampusapp.BuildConfig;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.managers.KinoManager;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
@PrepareForTest(KinoActivity.class)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
public class KinoActivityTest {
    private Cursor mockedDbCursor;
    private KinoManager mockedKinoManager;
    private KinoActivity kinoActivity;

    @Before
    public void setUp() {
        mockedDbCursor = PowerMockito.mock(Cursor.class);
        mockedKinoManager = PowerMockito.mock(KinoManager.class);
    }
    /**
     * Default usage - there are some movies
     * Expected output: default Kino activity layout
     */
    @Test
    public void mainComponentDisplayedTest() throws Exception{
        PowerMockito.when(mockedDbCursor.getCount()).thenReturn(1);
        PowerMockito.when(mockedKinoManager.getAllFromDb()).thenReturn(mockedDbCursor);

        PowerMockito.whenNew(KinoManager.class)
                    .withArguments(Mockito.any())
                    .thenReturn(mockedKinoManager);

        kinoActivity = Robolectric.buildActivity(KinoActivity.class).create().start().get();
        PowerMockito.verifyNew(KinoManager.class).withNoArguments();

        assertThat(kinoActivity.findViewById(R.id.drawer_layout).getVisibility()).isEqualTo(View.VISIBLE);
    }
}
