package de.tum.in.tumcampusapp.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

import de.tum.in.tumcampusapp.auxiliary.Utils;

public class Converters {
    @TypeConverter
    public static Date fromIso(String str) {
        return Utils.getDateTime(str);
    }

    @TypeConverter
    public static String fromDate(Date date) {
        return Utils.getDateString(date);
    }
}
