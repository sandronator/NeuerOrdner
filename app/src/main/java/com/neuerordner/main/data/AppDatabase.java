package com.neuerordner.main.data;

import android.database.SQLException;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;


@Database(entities = {Location.class, Item.class, ActiveLocation.class}, version = 4, exportSchema = false)
@TypeConverters({DateTimeConverter.class, DateConverter.class})
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

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
                db.execSQL("ALTER TABLE Item ADD COLUMN bestTillDate TEXT");
        }
    };
    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // Neue Tabelle mit Name NOT NULL
            db.execSQL(
                    "CREATE TABLE Item_new (" +
                            " Id TEXT NOT NULL PRIMARY KEY," +
                            " LocationId TEXT," +
                            " Name TEXT NOT NULL," +       // jetzt NOT NULL
                            " Quantity INTEGER NOT NULL," +
                            " Time TEXT," +
                            " bestTillDate TEXT," +
                            " FOREIGN KEY(LocationId) REFERENCES Location(Id) ON DELETE CASCADE" +
                            ")"
            );

            // Daten rüberkopieren, NULL-Namen durch '' oder einen Default ersetzen
            db.execSQL(
                    "INSERT INTO Item_new (Id, LocationId, Name, Quantity, Time, bestTillDate) " +
                            "SELECT Id, LocationId, COALESCE(Name, ''), Quantity, Time, bestTillDate FROM Item"
            );

            // Alte Tabelle löschen
            db.execSQL("DROP TABLE Item");

            // Neue umbenennen
            db.execSQL("ALTER TABLE Item_new RENAME TO Item");

            // Index wiederherstellen
            db.execSQL("CREATE INDEX IF NOT EXISTS index_Item_LocationId ON Item(LocationId)");
        }
    };


}
