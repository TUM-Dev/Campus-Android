package de.tum.`in`.tumcampusapp.utils.sync

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.sync.model.Sync
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncManagerTest {
    
    private lateinit var database: TcaDb
    private lateinit var syncManager: SyncManager
    
    private val syncDao: SyncDao by lazy {
        database.syncDao()
    }

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(), TcaDb::class.java)
                .allowMainThreadQueries()
                .build()
        syncManager = SyncManager(InstrumentationRegistry.getContext())
    }

    @After
    fun tearDown() {
        database.close()
    }

    /**
     * Default needSync usage - specific sync target hasn't been synced for a while
     * Expected output: returns true
     */
    @Test
    fun needSyncNeedsResyncTest() {
        val syncId = "needSyncNeedsResyncTest"
        syncDao.insert(Sync(syncId, DateTime(0)))
        assertThat(syncManager.needSync(syncId, 1234)).isTrue()
    }

    /**
     * Sync table is missing specified ID
     * Expected output: returns true
     */
    @Test
    fun needSyncNoIdTest() {
        val syncId = "needSyncNoIdTest"
        assertThat(syncManager.needSync(syncId, 1234)).isTrue()
    }

    /**
     * Last sync happened earlier than required
     * Expected output: returns false
     */
    @Test
    fun needSyncTooEarlyTest() {
        val syncId = "needSyncTooEarlyTest"
        syncDao.insert(Sync(syncId, DateTime.now()))
        assertThat(syncManager.needSync(syncId, 1234)).isFalse()
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
        val syncId = "replaceIntoDbNormal"
        syncDao.insert(Sync(syncId, DateTime(0)))
        assertThat(syncManager.needSync(syncId, 1234)).isTrue()
        syncManager.replaceIntoDb(syncId)
        assertThat(syncManager.needSync(syncId, 1234)).isFalse()
    }
}
