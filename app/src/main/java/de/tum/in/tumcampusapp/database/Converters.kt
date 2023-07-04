package de.tum.`in`.tumcampusapp.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.component.ui.alarm.model.FcmNotificationLocation
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization.DishPrices
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMember
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import de.tum.`in`.tumcampusapp.utils.tryOrNull
import org.joda.time.DateTime

class Converters {
    @TypeConverter
    fun isoToDateTime(str: String?): DateTime? {
        return if (str == null) null else DateTimeUtils.getDateTime(str)
    }

    @TypeConverter
    fun fromDateTime(date: DateTime?): String? {
        return if (date == null) null else DateTimeUtils.getDateTimeString(date)
    }

    @TypeConverter
    fun toJson(location: FcmNotificationLocation): String = Gson().toJson(location)

    @TypeConverter
    fun toLocation(json: String): FcmNotificationLocation = Gson().fromJson(json, FcmNotificationLocation::class.java)

    @TypeConverter
    fun fromMember(member: ChatMember?): String = Gson().toJson(member)

    @TypeConverter
    fun toMember(member: String): ChatMember? {
        return tryOrNull { Gson().fromJson(member, ChatMember::class.java) }
    }

    @TypeConverter
    fun fromDishPricesToString(dishPrices: DishPrices): String = Gson().toJson(dishPrices)

    @TypeConverter
    fun fromStringToDishPrices(json: String): DishPrices = Gson().fromJson(json, DishPrices::class.java)
}
