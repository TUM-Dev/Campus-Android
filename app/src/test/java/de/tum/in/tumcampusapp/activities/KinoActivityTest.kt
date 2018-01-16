package de.tum.`in`.tumcampusapp.activities

import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.adapters.KinoAdapter
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.database.dao.KinoDao
import de.tum.`in`.tumcampusapp.models.tumcabe.Kino
import de.tum.`in`.tumcampusapp.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.repository.KinoRemoteRepository
import de.tum.`in`.tumcampusapp.viewmodel.KinoViewModel
import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers

import org.assertj.core.api.Assertions.assertThat

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class KinoActivityTest {
    private var kinoActivity: KinoActivity? = null
    private lateinit var dao: KinoDao
    private lateinit var viewModel:KinoViewModel

    private lateinit var immediate: Scheduler

    @Before
    fun setUp() {
        val db =  TcaDb.getInstance(RuntimeEnvironment.application)
        KinoLocalRepository.db = db
        viewModel = KinoViewModel(KinoLocalRepository,KinoRemoteRepository, CompositeDisposable())
        immediate = object : Scheduler() {
            override fun scheduleDirect(run: Runnable,
                                        delay: Long, unit: TimeUnit): Disposable {
                return super.scheduleDirect(run, 0, unit)
            }

            override fun createWorker(): Scheduler.Worker {
                return ExecutorScheduler.ExecutorWorker(
                        Executor { it.run() })
            }
        }
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline()  }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline()  }
        RxJavaPlugins.setSingleSchedulerHandler { Schedulers.trampoline()  }
        RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
        dao = db.kinoDao()
        dao.flush()
    }

    @After
    fun tearDown() {
        TcaDb.getInstance(RuntimeEnvironment.application).close()
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }

    /**
     * Default usage - there are some movies
     * Expected output: default Kino activity layout
     */
    @Test
    fun mainComponentDisplayedTest() {
        dao.insert(Kino())
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
        //For some reason the ui needs a while until it's been updated.
        Thread.sleep(100)
        assertThat(kinoActivity!!.findViewById<View>(R.id.no_movies_layout).visibility).isEqualTo(View.VISIBLE)
    }

    /**
     * There are movies available
     * Expected output: KinoAdapter is used for pager.
     */
    @Test
    fun kinoAdapterUsedTest() {
        dao.insert(Kino())
        kinoActivity = Robolectric.buildActivity(KinoActivity::class.java).create().start().get()
        waitForUI()

        assertThat((kinoActivity!!.findViewById<View>(R.id.pager) as ViewPager).adapter!!.javaClass).isEqualTo(KinoAdapter::class.java)
    }

    /**
     * Since we have an immediate scheduler which runs on the same thread and thus can only execute actions sequentially, this will
     * make the test wait until any previous tasks (like the activity waiting for kinos) are done.
     */
    private fun waitForUI(){
        viewModel.getAllKinos().blockingFirst()
    }
}
