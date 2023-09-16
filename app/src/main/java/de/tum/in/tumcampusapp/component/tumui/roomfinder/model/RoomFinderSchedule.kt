package de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model

@Deprecated(
"""should be removed
 Logic from WeekViewFragment and RoomFinderSchedule should be reimplemented
 in new NavigationDetailsFragment, instead of legacy RoomFinderDetailsActivity.
 More info: https://github.com/TUM-Dev/Campus-Android/pull/1462"
"""
)
data class RoomFinderSchedule(
    var start: String = "", // TODO: these should probably be DateTimes instead of Strings
    var end: String = "",
    var event_id: Long = -1,
    var title: String = ""
)
