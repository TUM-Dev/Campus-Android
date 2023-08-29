package de.tum.`in`.tumcampusapp.activities

import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.viewpager.widget.ViewPager
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.TestApp
import de.tum.`in`.tumcampusapp.component.ui.tufilm.KinoActivity
import de.tum.`in`.tumcampusapp.component.ui.tufilm.KinoAdapter
import de.tum.`in`.tumcampusapp.component.ui.tufilm.KinoDao
import de.tum.`in`.tumcampusapp.component.ui.tufilm.KinoViewModel
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.database.TcaDb
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Ignore
@RunWith(RobolectricTestRunner::class)
@Config(application = TestApp::class)
class KinoActivityTest {
    private var kinoActivity: KinoActivity? = null
    private lateinit var dao: KinoDao
    private lateinit var viewModel: KinoViewModel

    @Before
    fun setUp() {
        val db = TcaDb.getInstance(ApplicationProvider.getApplicationContext())
        val localRepository = KinoLocalRepository(db)
        viewModel = KinoViewModel(localRepository)
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setSingleSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
        dao = db.kinoDao()
        dao.flush()
    }

    @After
    fun tearDown() {
        TcaDb.getInstance(ApplicationProvider.getApplicationContext()).close()
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }

    /**
     * Default usage - there are some movies
     * Expected output: default Kino activity layout
     */
    @Test
    fun mainComponentDisplayedTest() {
        dao.insert(KINO)
        kinoActivity = Robolectric.buildActivity(KinoActivity::class.java).create().start().get()
        waitForUI()
        assertThat(kinoActivity!!.findViewById<View>(R.id.drawer_layout).visibility).isEqualTo(View.VISIBLE)
    }

    /**
     * There are no movies to display
     * Expected output: no movies layout displayed
     */
    @Test
    fun mainComponentNoMoviesDisplayedTest() {
        kinoActivity = Robolectric.buildActivity(KinoActivity::class.java).create().start().get()
        waitForUI()
        // For some reason the ui needs a while until it's been updated.
        Thread.sleep(100)
        assertThat(kinoActivity!!.findViewById<View>(R.id.layout_error).visibility).isEqualTo(View.VISIBLE)
    }

    /**
     * There are movies available
     * Expected output: KinoAdapter is used for pager.
     */
    @Test
    fun kinoAdapterUsedTest() {
        dao.insert(KINO)
        kinoActivity = Robolectric.buildActivity(KinoActivity::class.java).create().start().get()
        waitForUI()
        Thread.sleep(100)
        assertThat((kinoActivity!!.findViewById<View>(R.id.pager) as ViewPager).adapter!!.javaClass).isEqualTo(KinoAdapter::class.java)
    }

    /**
     * Since we have an immediate scheduler which runs on the same thread and thus can only execute actions sequentially, this will
     * make the test wait until any previous tasks (like the activity waiting for kinos) are done.
     */
    private fun waitForUI() {
        viewModel.getAllKinos().blockingFirst()
    }

    companion object {
        private val KINO = Kino(
            "123",
            "Deadpool 2",
            "2018",
            "137 min",
            "Comedy",
            "Someone",
            "Ryan Reynolds and others",
            "The best",
            "I dunno stuff happens",
            "",
            null,
            DateTime.now(),
            DateTime.now(),
            ""
        )
    }
}
