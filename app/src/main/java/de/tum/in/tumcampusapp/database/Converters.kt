package de.tum.`in`.tumcampusapp.database

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.component.ui.alarm.model.FcmNotificationLocation
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMember
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import org.joda.time.DateTime

class Converters {
    @TypeConverter
    fun isoToDateTime(str: String?): DateTime? {
        return if (str != null) {
            DateTimeUtils.getDateTime(str)
        } else {
            null
        }
    }

    @TypeConverter
    fun fromDateTime(date: DateTime?): String? {
        return if (date != null) {
            DateTimeUtils.getDateTimeString(date)
        } else {
            null
        }
    }

    @TypeConverter
    fun toJson(location: FcmNotificationLocation): String = Gson().toJson(location);

    @TypeConverter
    fun toLocation(json: String): FcmNotificationLocation = Gson().fromJson(json, FcmNotificationLocation::class.java)

    @TypeConverter
    fun fromMember(member: ChatMember): String = Gson().toJson(member)

    @TypeConverter
    fun toMember(member: String): ChatMember = Gson().fromJson<ChatMember>(member, ChatMember::class.java)
}
