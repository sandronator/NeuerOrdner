package com.example.neuerordner.data;

import android.content.Context;

import androidx.room.Room;

public class CreateDatabase {
    public static volatile AppDatabase DB;

    public static AppDatabase fetch(Context ctx) {
        if (DB == null) {
            //Thread Safe
            synchronized (CreateDatabase.class) {
                if  (DB == null) {
                    DB = Room.databaseBuilder(ctx.getApplicationContext(), AppDatabase.class, "app-db").enableMultiInstanceInvalidation().allowMainThreadQueries().build();
                }
            }
        }
        return DB;
    }
}
