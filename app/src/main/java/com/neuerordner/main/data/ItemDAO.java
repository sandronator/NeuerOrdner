package com.neuerordner.main.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ItemDAO {
    @Query("SELECT * FROM item")
    List<Item> getAll();

    @Query("SELECT * FROM item")
    List<Item> getSyncedAll();
    @Insert
    void insert(Item item);

    @Delete
    void delete(Item item);

    @Query("DELETE FROM Item Where Id = :itemId")
    void delete(String itemId);

    @Query("SELECT * FROM item Where Id = :id LIMIT 1")
    Item get(String id);

    @Query("SELECT * FROM item Where Id = :id LIMIT 1")
    LiveData<Item> getLiveData(String id);

    @Update
    Integer update(Item item);

    @Query("SELECT * FROM item WHERE LocationId = :locId")
    LiveData<List<Item>> getAllFromLocation(String locId);

    @Query("SELECT * FROM item WHERE LocationId = :locId")
    List<Item> getAllFromLocationSynced(String locId);
}
