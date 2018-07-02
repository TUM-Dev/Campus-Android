package de.tum.`in`.tumcampusapp.database

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.component.ui.alarm.model.FcmNotificationLocation
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMember
import de.tum.`in`.tumcampusapp.utils.DateUtils
import java.util.*

class Converters {
    @TypeConverter
    fun fromIso(str: String): Date = DateUtils.getDateTime(str)

    @TypeConverter
    fun fromDate(date: Date): String = DateUtils.getDateTimeString(date)

    @TypeConverter
    fun toJson(location: FcmNotificationLocation): String = Gson().toJson(location);

    @TypeConverter
    fun toLocation(json: String): FcmNotificationLocation = Gson().fromJson(json, FcmNotificationLocation::class.java)

    @TypeConverter
    fun fromMember(member: ChatMember): String {
        return Gson().toJson(member)
    }

    @TypeConverter
    fun toMember(member: String): ChatMember {
        return Gson().fromJson<ChatMember>(member, ChatMember::class.java!!)
    }
}
