package com.example.neuerordner.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LocationDAO {
    @Query("SELECT * FROM location")
    List<Location> getAll();

    @Insert
    void insert(Location location);

    @Delete
    void delete(Location location);

    @Query("DELETE FROM location Where id = :id")
    void delete(String id);

    @Update
    int update(Location location);

    @Query("SELECT * FROM location WHERE Id = :locId")
    Location get(String locId);

}
