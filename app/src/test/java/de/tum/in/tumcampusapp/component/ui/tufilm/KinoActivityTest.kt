package de.tum.`in`.tumcampusapp.component.ui.tufilm

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.news.KinoViewModel
import de.tum.`in`.tumcampusapp.component.ui.news.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.news.repository.KinoRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import de.tum.`in`.tumcampusapp.database.TcaDb
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KinoActivityTest {

    @get:Rule
    val rule = ActivityTestRule(KinoActivity::class.java)
    
    private lateinit var database: TcaDb

    private val kinoDao: KinoDao by lazy {
        KinoLocalRepository.db = database
        database.kinoDao()
    }

    private val viewModel: KinoViewModel by lazy {
        KinoViewModel(KinoLocalRepository, KinoRemoteRepository, CompositeDisposable())
    }

    @BeforeClass
    fun setupRxJava() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline()  }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline()  }
        RxJavaPlugins.setSingleSchedulerHandler { Schedulers.trampoline()  }
        RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(), TcaDb::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }
    
    @AfterClass
    fun resetRxJava() {
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }

    /**
     * Default usage - there are some movies
     * Expected output: default Kino activity layout
     */
    @Test
    fun mainComponentDisplayedTest() {
        kinoDao.insert(Kino())

        waitForUI()

        onView(ViewMatchers.withId(R.id.pager))
                .check(matches(isDisplayed()))
    }

    /**
     * There are no movies to display
     * Expected output: no movies layout displayed
     */
    @Test
    fun mainComponentNoMoviesDisplayedTest() {
        waitForUI()

        //For some reason the UI needs a while until it's been updated.
        Thread.sleep(100)

        onView(ViewMatchers.withId(R.id.error_layout))
                .check(matches(isDisplayed()))
    }

    /**
     * Since we have an immediate scheduler which runs on the same thread and thus can only execute
     * actions sequentially, this will make the test wait until any previous tasks (like the
     * activity waiting for kinos) are done.
     */
    private fun waitForUI(){
        viewModel.getAllKinos().blockingFirst()
    }

}
