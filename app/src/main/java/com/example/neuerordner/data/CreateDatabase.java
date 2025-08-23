package com.example.neuerordner.data;

import android.content.Context;

import androidx.room.Room;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                            .addMigrations(AppDatabase.MIGRATION_1_2)
                            .build();
                }
            }
        }
        return DB;
    }
}
