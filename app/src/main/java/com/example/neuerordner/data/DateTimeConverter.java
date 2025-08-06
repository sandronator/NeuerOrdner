package com.example.neuerordner.data;

import androidx.room.TypeConverter;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeConverter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @TypeConverter
    public static OffsetDateTime DateTimeFromString(String dateTimeString) {
        return dateTimeString == null ? null : OffsetDateTime.parse(dateTimeString, formatter);
    }
    @TypeConverter
    public static String DateTimeToString(OffsetDateTime offdatetime) {
        return offdatetime == null ? null : offdatetime.format(formatter);
    }
}
