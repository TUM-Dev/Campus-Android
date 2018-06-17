package de.tum.`in`.tumcampusapp.database

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.component.ui.alarm.model.GCMNotificationLocation
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMember
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import org.joda.time.DateTime

class Converters {
    @TypeConverter
    fun isoToDateTime(str: String): DateTime = DateTimeUtils.getDateTime(str)

    @TypeConverter
    fun fromDateTime(date: DateTime): String = DateTimeUtils.getDateTimeString(date)

    @TypeConverter
    fun toJson(location: GCMNotificationLocation): String = Gson().toJson(location);

    @TypeConverter
    fun toLocation(json: String): GCMNotificationLocation = Gson().fromJson(json, GCMNotificationLocation::class.java)

    @TypeConverter
    fun fromMember(member: ChatMember): String = Gson().toJson(member)

    @TypeConverter
    fun toMember(member: String): ChatMember = Gson().fromJson<ChatMember>(member, ChatMember::class.java)
}
