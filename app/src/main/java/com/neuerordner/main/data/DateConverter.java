package com.neuerordner.main.data;

import android.util.Log;

import androidx.room.TypeConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class DateConverter {
    private static final DateFormat DATE_FORMATTER = SimpleDateFormat.getDateInstance();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @TypeConverter
    public static Date parseDate(String value) {
        try {
            return DATE_FORMATTER.parse(value);
        } catch (ParseException pex) {
            Log.e("Error Parsing Date", pex.getMessage());
            return null;
        } catch (NullPointerException nex) {
            Log.e("Nullpointer Exception", nex.getMessage());
            return null;
        }
    }

    @TypeConverter
    public static LocalDate parseLocalDate(String value) {
        if (value == null) return null;
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException pex) {
            Log.e("Error Parsing Date", pex.getMessage());
            return null;
        }
    }

    @TypeConverter
    public static String convertDate (Date value) {
        return DATE_FORMATTER.format(value);
    }

    @TypeConverter
    public static String convertDateTime (LocalDate value) {
        return DATE_TIME_FORMATTER.format(value);
    }


}
