package com.neuerordner.main.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Entity(
        tableName = "activelocation"

)
@Dao
public interface ActiveLocationDAO {
    @Query("SELECT * FROM activelocation")
    List<ActiveLocation> getAll();

    @Insert
    void insert(ActiveLocation activeLocation);

    @Update
    void update(ActiveLocation activeLocation);

    @Delete
    void delete(ActiveLocation activeLocation);
}
