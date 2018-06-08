package de.tum.`in`.tumcampusapp.utils

import org.joda.time.DateTime
import java.util.*

fun Date.toJoda() = DateTime(this.time)