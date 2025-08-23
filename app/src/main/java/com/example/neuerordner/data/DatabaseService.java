package com.example.neuerordner.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

//Maybe extending from FileService and make Fileservice private????
public class DatabaseService<T> {
    private final AppDatabase _db;
    /**
    Utitlity Class to work with {@link AppDatabase}. Purpose is for Dumping and Updating existing
     *Local Room Database. To work with it we need
     * @param ctx from the current Context
     **/
    public DatabaseService(Context ctx){
        _db = CreateDatabase.fetch(ctx);
    }

    public List<Location> getAllLocations() {
        return _db.locationDAO().getAll();
    }
    public Map<Location, List<Item>> getAllLocationsAndItems() {
        List<Location> allLocations = this.getAllLocations();
        List<Item> allItems = this.getAllItems();
        Map<Location,List<Item>> locationItemList = new HashMap<>();
        for (Location location: allLocations) {
            List<Item> matchedItems = allItems.stream().filter(item -> item.LocationId == location.Id).collect(Collectors.toList());
            locationItemList.put(location, matchedItems);
        }
        return locationItemList;
    }

    public Location getLocation(String id) {
        return _db.locationDAO().get(id);
    }

    public void deleteLocations(List<String> locationIds) {
        locationIds.forEach(locationId -> {
            _db.locationDAO().delete(locationId);
        });
    }

    public void deleteLocation(Location location) {
        _db.locationDAO().delete(location);
    }

    private void updateLocations(List<Location> locations) {
        for (Location location: locations
             ) {
            _db.locationDAO().update(location);
        }
    }

    public void addLocations(List<Location> locations) {
        for (Location location: locations
        ) {
            addLocation(location);
        }
    }
    public void addLocation(Location location) {
        _db.locationDAO().insert(location);
    }

    public boolean updateLocation(Location location) {
        Integer updatedRows = _db.locationDAO().update(location);
        if (updatedRows > 0) return true;
        else return false;
    }

    public void deleteItems(List<String> itemIds) {
        itemIds.forEach(itemId -> {
            _db.itemDao().delete(itemId);
        });
    }

    public List<Item> getAllItems() {
        return _db.itemDao().getSyncedAll();
    }

    public List<Item> getAllItemsFromLocation(String locationId) {
        return _db.itemDao().getAllFromLocationSynced(locationId);
    }
    public Item getItem(String id) {
        return _db.itemDao().get(id);
    }

    public LiveData<Item> getItemLiveData(String id) {
        return _db.itemDao().getLiveData(id);
    }

    public void deleteItem(Item item) {
        _db.itemDao().delete(item);
    }

    public Map<Item, Boolean> updateItems(List<Item> items) {
        Map<Item, Boolean> successItems = new HashMap<Item, Boolean>();
        items.forEach(item -> {
            Boolean successfull = _db.itemDao().update(item) > 0;
            successItems.put(item, successfull);
        });
        return successItems;
    }
    public boolean uddateItem(Item item) {
        Integer succesfull = _db.itemDao().update(item);
        if (succesfull > 0) return true;
        else return false;
    }

    public void addItems(List<Item> items) {
        items.forEach(item -> {
            addItem(item);
        });
    }
    public void addItem(Item item) {
        _db.itemDao().insert(item);
    }

    private void InsertLocationItemEntry(Location location, List<Item> itemList) {
        _db.locationDAO().insert(location);

        itemList.forEach(item -> {
            item.LocationId = location.Id;
            _db.itemDao().insert(item);
        });
    }
    public void EraseAndSetupNew(Map<Location, List<Item>> locationItems) throws Exception {
        _db.locationDAO().getAll().forEach(location -> {
            _db.locationDAO().delete(location);
        });

        _db.itemDao().getSyncedAll().forEach(item -> {
            _db.itemDao().delete(item);
        });

        InsertIntoDatabase(locationItems);
    }

    public void UpdateAndInsert(Map<Location, List<Item>> locationItems) throws Exception {
        UpdateDatabase(locationItems);
        InsertIntoDatabase(locationItems);
    }

    public void InsertIntoDatabase(Map<Location, List<Item>> locationItems) throws Exception {
        if (locationItems.isEmpty()) {
            return;
        }
        Set<String> locationNamesInDbSet = getAllLocations().stream().map(location -> location.Name).collect(Collectors.toSet());


        if (locationNamesInDbSet.isEmpty()) {
            locationItems.forEach((location, itemList) -> {
                InsertLocationItemEntry(location, itemList);
            });
        } else {
            // Redundanz Check
            locationItems.forEach((location, itemList) -> {
                if (!locationNamesInDbSet.contains(location.Name)) {
                    InsertLocationItemEntry(location, itemList);
                }
            });
        }

    }
    public void close()  {
        this._db.close();
    }

    public void UpdateDatabase(Map<Location, List<Item>> locationItems) throws Exception {
         if (locationItems.isEmpty()) {
             return;
         }

         List<Location> currentLocations = getAllLocations();
         Set<Item> allItems = _db.itemDao().getSyncedAll().stream().collect(Collectors.toSet());

         for (Location location : currentLocations) {

             if (!locationItems.keySet().contains(location.Name)) {
                 continue;
             }
             List<Item> items = locationItems.get(location.Name);
             if (items.isEmpty()) {
                 continue;
             }

             for (Item item : items) {
                if (allItems.contains(item)) {
                    _db.itemDao().update(item);
                } else {
                    _db.itemDao().insert(item);
                }
             }
         }
    }

}
