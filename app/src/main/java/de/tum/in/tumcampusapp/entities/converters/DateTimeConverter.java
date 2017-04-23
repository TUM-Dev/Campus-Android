package de.tum.in.tumcampusapp.entities.converters;

import org.joda.time.DateTime;

import java.util.Date;

import io.objectbox.converter.PropertyConverter;

public class DateTimeConverter implements PropertyConverter<DateTime, Date> {
    @Override
    public DateTime convertToEntityProperty(Date e) {
        if (e == null) {
            return null;
        }

        return new DateTime(e);
    }

    @Override
    public Date convertToDatabaseValue(DateTime e) {
        return e.toDate();
    }
}
