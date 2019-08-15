package de.tum.`in`.tumcampusapp.utils.sync

import android.content.Context
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.sync.model.Sync
import org.joda.time.DateTime

/**
 * Sync Manager, tracks last successful syncs and prevents api fetch spams
 */
class SyncManager(context: Context) {

    private val dao: SyncDao = TcaDb.getInstance(context).syncDao()

    /**
     * Checks if a new sync is needed or if data is up-to-date
     *
     * @param obj     Gives class name as sync ID
     * @param seconds Sync period, e.g. 86400 for 1 day
     * @return true if sync is needed, else false
     */
    fun needSync(obj: Any, seconds: Int): Boolean {
        return needSync(obj.javaClass.name, seconds)
    }

    /**
     * Checks if a new sync is needed or if data is up-to-date
     *
     * @param id      Sync-ID (derived by originator class name)
     * @param seconds Sync period, e.g. 86400 for 1 day
     * @return true if sync is needed, else false
     */
    fun needSync(id: String, seconds: Int): Boolean {
        return dao.getSyncSince(id, seconds) == null
    }

    /**
     * Replace or Insert a successful sync event in the database
     *
     * @param obj Gives class name as sync ID
     */
    fun replaceIntoDb(obj: Any) {
        replaceIntoDb(obj.javaClass.name)
    }

    /**
     * Replace or Insert a successful sync event in the database
     *
     * @param id Sync-ID (derived by originator class name)
     */
    fun replaceIntoDb(id: String) {
        if (id.isEmpty()) {
            return
        }
        dao.insert(Sync(id, DateTime.now()))
    }

    /**
     * Removes all items from database
     */
    fun deleteFromDb() {
        dao.removeCache()
    }
}