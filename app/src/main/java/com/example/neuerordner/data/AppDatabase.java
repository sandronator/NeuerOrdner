package com.example.neuerordner.data;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;


@Database(entities = {Location.class, Item.class, ActiveLocation.class}, version = 2, exportSchema = false)
@TypeConverters({DateTimeConverter.class})
public abstract class   AppDatabase extends RoomDatabase {
    public abstract LocationDAO locationDAO();
    public abstract ItemDAO itemDao();
    public abstract  ActiveLocationDAO activeLocationDao();

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {

            // Neue Tabele erstellen
            db.execSQL("CREATE TABLE IF NOT EXISTS LocationsZwischenspeicher(" +
                    " Id TEXT NOT NULL PRIMARY KEY," +
                    " Name TEXT NOT NULL," +
                    " CreationDate TEXT NOT NULL DEFAULT '') ");

            db.execSQL("INSERT INTO LocationsZwischenspeicher(Id, Name) " +
                    " SELECT ID, COALESCE(Name, '') " +
                    " FROM Location ");
            db.execSQL("DROP TABLE Location");
            db.execSQL("ALTER TABLE LocationsZwischenspeicher RENAME TO Location");

            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_Location_Name ON Location(Name)");


            db.execSQL("UPDATE Location " +
                    "SET CreationDate = strftime('%Y-%m-%dT%H:%M:%fZ', datetime('now')) " +
                    "WHERE CreationDate = ''");
        }
    };
}
