package de.tum.in.tumcampusapp.managers;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import de.tum.in.tumcampusapp.TestApp;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.sync.SyncDao;
import de.tum.in.tumcampusapp.utils.sync.SyncManager;
import de.tum.in.tumcampusapp.utils.sync.model.Sync;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
@RunWith(RobolectricTestRunner.class)
@Config(application = TestApp.class)
public class SyncManagerTest {
    private SyncManager syncManager;
    private SyncDao dao;

    @Before
    public void setUp() {
        dao = TcaDb.Companion.getInstance(RuntimeEnvironment.application)
                             .syncDao();
        dao.removeCache();
        syncManager = new SyncManager(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() {
        TcaDb.Companion.getInstance(RuntimeEnvironment.application)
                       .close();
    }

    /**
     * Default needSync usage - specific sync target hasn't been synced for a while
     * Expected output: returns true
     */
    @Test
    public void needSyncNeedsResyncTest() {
        String sync_id = "needSyncNeedsResyncTest";
        dao.insert(new Sync(sync_id, new DateTime(0)));
        assertThat(syncManager.needSync(sync_id, 1234)).isTrue();
    }

    /**
     * Sync table is missing specified ID
     * Expected output: returns true
     */
    @Test
    public void needSyncNoIdTest() {
        String sync_id = "needSyncNoIdTest";
        assertThat(syncManager.needSync(sync_id, 1234)).isTrue();
    }

    /**
     * Last sync happened earlier than required
     * Expected output: returns false
     */
    @Test
    public void needSyncTooEarlyTest() {
        String sync_id = "needSyncTooEarlyTest";
        dao.insert(new Sync(sync_id, DateTime.now()));
        assertThat(syncManager.needSync(sync_id, 1234)).isFalse();
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
    public void replaceIntoDbNormal() {
        String sync_id = "replaceIntoDbNormal";
        dao.insert(new Sync(sync_id, new DateTime(0)));
        assertThat(syncManager.needSync(sync_id, 1234)).isTrue();
        syncManager.replaceIntoDb(sync_id);
        assertThat(syncManager.needSync(sync_id, 1234)).isFalse();
    }
}
