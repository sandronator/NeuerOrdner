package com.neuerordner.main.data;

import android.content.Context;

import androidx.room.Room;

public class CreateDatabase {
    public static volatile AppDatabase DB;
    public static AppDatabase fetch(Context ctx) {
        if (DB == null) {
            //Thread Safe
            synchronized (CreateDatabase.class) {
                if  (DB == null) {
                    DB = Room.databaseBuilder(
                            ctx.getApplicationContext(),
                            AppDatabase.class,
                            "app-db")

                            .enableMultiInstanceInvalidation()
                            .allowMainThreadQueries()
                            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6)
                            .build();
                }
            }
        }
        return DB;
    }
}
