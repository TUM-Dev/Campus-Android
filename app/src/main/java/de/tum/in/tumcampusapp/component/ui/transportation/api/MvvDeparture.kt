package de.tum.`in`.tumcampusapp.component.ui.transportation.api

import org.joda.time.DateTime

data class MvvDeparture(
    val servingLine: MvvServingLine = MvvServingLine(),
    val dateTime: DateTime = DateTime(),
    val countdown: Int = 0
)
