package de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model

data class RoomFinderSchedule(var start: String = "", // TODO: these should probably be DateTimes instead of Strings
                              var end: String = "",
                              var event_id: Long = -1,
                              var title: String = "")
