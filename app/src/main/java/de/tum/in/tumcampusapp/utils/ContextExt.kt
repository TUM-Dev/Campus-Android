package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.database.TcaDb

val Context.tcaDb: TcaDb
    get() = TcaDb.getInstance(this)

val Context.tumCabeClient: TUMCabeClient
    get() = TUMCabeClient.getInstance(this)
