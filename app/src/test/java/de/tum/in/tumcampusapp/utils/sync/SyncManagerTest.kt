package de.tum.`in`.tumcampusapp.utils.sync

import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.TestApp
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.sync.model.Sync
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
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
class SyncManagerTest {
    private var syncManager: SyncManager? = null
    private var dao: SyncDao? = null

    @Before
    fun setUp() {
        dao = TcaDb.getInstance(RuntimeEnvironment.application)
                .syncDao()
        dao!!.removeCache()
        syncManager = SyncManager(RuntimeEnvironment.application)
    }

    @After
    fun tearDown() {
        TcaDb.getInstance(RuntimeEnvironment.application)
                .close()
    }

    /**
     * Default needSync usage - specific sync target hasn't been synced for a while
     * Expected output: returns true
     */
    @Test
    fun needSyncNeedsResyncTest() {
        val sync_id = "needSyncNeedsResyncTest"
        dao!!.insert(Sync(sync_id, DateTime(0)))
        assertThat(syncManager!!.needSync(sync_id, 1234)).isTrue()
    }

    /**
     * Sync table is missing specified ID
     * Expected output: returns true
     */
    @Test
    fun needSyncNoIdTest() {
        val sync_id = "needSyncNoIdTest"
        assertThat(syncManager!!.needSync(sync_id, 1234)).isTrue()
    }

    /**
     * Last sync happened earlier than required
     * Expected output: returns false
     */
    @Test
    fun needSyncTooEarlyTest() {
        val sync_id = "needSyncTooEarlyTest"
        dao!!.insert(Sync(sync_id, DateTime.now()))
        assertThat(syncManager!!.needSync(sync_id, 1234)).isFalse()
    }

    /**
     * Non direct way of checking that Sync entity has been replaced through needSync
     * Workflow:
     * 1. add sync with specific id and "old" timestamp
     * 2. verify needSync returns true
     * 3. use target function to update same id
     * 4. verify needSync returns false
     */
    @Test
    fun replaceIntoDbNormal() {
        val sync_id = "replaceIntoDbNormal"
        dao!!.insert(Sync(sync_id, DateTime(0)))
        assertThat(syncManager!!.needSync(sync_id, 1234)).isTrue()
        syncManager!!.replaceIntoDb(sync_id)
        assertThat(syncManager!!.needSync(sync_id, 1234)).isFalse()
    }
}
