package com.neuerordner.main.data;

import android.util.Log;

import androidx.room.TypeConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter {
    private static final DateFormat FORMATTER = SimpleDateFormat.getDateInstance();

    @TypeConverter
    public static Date parse(String value) {
        try {
            return FORMATTER.parse(value);
        } catch (ParseException pex) {
            Log.e("Error Parsing Date", pex.getMessage());
            return null;
        } catch (NullPointerException nex) {
            Log.e("Nullpointer Exception", nex.getMessage());
            return null;
        }
    }

    @TypeConverter
    public static String convert (Date value) {
        return FORMATTER.format(value);
    }

}
