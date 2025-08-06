package com.example.neuerordner.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;


@Database(entities = {Location.class, Item.class, ActiveLocation.class}, version = 1, exportSchema = false)
@TypeConverters({DateTimeConverter.class})
public abstract class   AppDatabase extends RoomDatabase {
    public abstract LocationDAO locationDAO();
    public abstract ItemDAO itemDao();
    public abstract  ActiveLocationDAO activeLocationDao();
}
