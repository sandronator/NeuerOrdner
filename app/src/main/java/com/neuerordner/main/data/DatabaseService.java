package com.neuerordner.main.data;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        _db.locationDAO().getAll().forEach(l -> {
            Log.d("Location Name After insert: ", l.Name);
        });

    }
    public void EraseAndSetupNew(Map<String, List<Item>> locationItems) {
        _db.locationDAO().getAll().forEach(location -> {
            _db.locationDAO().delete(location);
        });

        _db.itemDao().getSyncedAll().forEach(item -> {
            _db.itemDao().delete(item);
        });

        InsertHashMap(locationItems);
    }

    public void UpdateAndInsert(Map<String, List<Item>> locationItems) {
        UpdateDatabase(locationItems);
        InsertHashMap(locationItems);
    }

    private boolean LocationExist(String locationName) {
        Set<String> locations = _db.locationDAO().getAll().stream().map(l -> l.Name).collect(Collectors.toSet());
        if (locations.contains(locationName)) return true;
        return false;
    }

    public void InsertHashMap(Map<String, List<Item>> locationItems) {
        if (locationItems.isEmpty()) {
            return;
        }

        try {
            for (Map.Entry<String, List<Item>> entry: locationItems.entrySet()) {
                String locName = entry.getKey();
                if (LocationExist(locName)) {
                    continue;
                }
                Location location = new Location(locName);
                InsertLocationItemEntry(location, entry.getValue());
            }
        } catch (Exception e) {
            Log.e("Error Inserting HashMap", e.toString());
        }
    }
    public void close()  {
        this._db.close();
    }

    public void UpdateDatabase(Map<String, List<Item>> locationItems) {
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
