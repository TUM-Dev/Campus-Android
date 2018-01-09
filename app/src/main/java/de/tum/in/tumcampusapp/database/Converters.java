package de.tum.in.tumcampusapp.database;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;

import java.util.Date;

import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;

public class Converters {
    @TypeConverter
    public static Date fromIso(String str) {
        return Utils.getISODateTime(str);
    }

    @TypeConverter
    public static String fromDate(Date date) {
        return Utils.getDateString(date);
    }

    @TypeConverter
    public static String fromMember(ChatMember member) {return new Gson().toJson(member);}

    @TypeConverter
    public static ChatMember toMember(String member) {return new Gson().fromJson(member, ChatMember.class);}
}
