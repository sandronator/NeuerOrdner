package com.neuerordner.main.data;

import androidx.room.TypeConverter;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeConverter {

    private static final DateTimeFormatter ISO_T =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME;          // 2025-08-07T15:00:42Z
    private static final DateTimeFormatter ISO_SPACE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX"); // 2025-08-07 15:00:42Z

    @TypeConverter
    public static OffsetDateTime DateTimeFromString(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return OffsetDateTime.parse(value, ISO_T);       // bevorzugt korrektes Format
        } catch (DateTimeParseException e) {
            return OffsetDateTime.parse(value, ISO_SPACE);   // Fallback für Leerzeichen
        }
    }

    @TypeConverter
    public static String DateTimeToString(OffsetDateTime dt) {
        return dt == null ? "" : ISO_T.format(dt);           // schreibt künftig immer „T“
    }
}
