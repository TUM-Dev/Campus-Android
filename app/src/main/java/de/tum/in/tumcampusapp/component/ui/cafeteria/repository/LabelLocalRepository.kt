package de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository

import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization.Label
import de.tum.`in`.tumcampusapp.database.TcaDb
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

class LabelLocalRepository @Inject constructor(private val tumCabeClient: TUMCabeClient, private val db: TcaDb) {

    private val executor: Executor = Executors.newSingleThreadExecutor()

    fun clear() = this.db.labelDao().removeCache()

    fun updateLastSync() {}

    fun addLabels(labels: List<Label>) = this.executor.execute {
        this.db.labelDao().insert(labels)
    }

}