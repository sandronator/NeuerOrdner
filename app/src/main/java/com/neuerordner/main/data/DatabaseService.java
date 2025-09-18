package com.neuerordner.main.data;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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
        List<Location> locations = _db.locationDAO().getAll();
        if (locations == null) return new ArrayList<>();
        Collections.sort(locations);
        return locations;
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
            if (LocationExist(location.Name, location.Id)) continue;
            addLocation(location);
        }
    }
    public void addLocation(Location location) throws IllegalArgumentException {
        if (LocationExist(location.Name, location.Id)) throw new IllegalArgumentException("Location already exists");
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
        List<Item> items = _db.itemDao().getAll();
        if (items == null) return new ArrayList<>();
        Collections.sort(items);
        return items;
    }

    public List<Item> getAllItemsFromLocation(String locationId) {
        List<Item> items = _db.itemDao().getAllFromLocationSynced(locationId);
        if (items == null) return new ArrayList<>();
        Collections.sort(items);
        return items;
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
        if (itemExist(item.Id, item.Name)) return;
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

    public void InsertAndUpdate(Map<String, List<Item>> locationItems) {
        InsertHashMap(locationItems);
        UpdateDatabase(locationItems);
    }

    private boolean LocationExist(String locationName, String Id) {
        Location location = _db.locationDAO().get(Id);
        boolean idMatch = location != null ? true : false;

        Set<String> nameSet = _db.locationDAO().getAll().stream().map(loc -> loc.Name).collect(Collectors.toSet());
        boolean nameMatch = nameSet.contains(locationName) ? true : false;

        return idMatch || nameMatch;
    }
    private boolean itemExist(String id, String name) {
        Item item = _db.itemDao().get(id);
        boolean idMatch = item != null ? true : false;
        Set<String> nameSet = _db.itemDao().getAll().stream().map(loc -> loc.Name).collect(Collectors.toSet());
        boolean nameMatch = nameSet.contains(name) ? true : false;
        return idMatch || nameMatch;
    }

    public void InsertHashMap(Map<String, List<Item>> locationItems) {
        if (locationItems.isEmpty()) {
            return;
        }
        List<Item> items = new ArrayList<>();
        try {
            for (Map.Entry<String, List<Item>> entry: locationItems.entrySet()) {
                String key = entry.getKey();
                if (hashMapKeyFailCheck(key)) continue;
                String locationId = key.split(",")[0];
                String locName = key.split(",")[1];

                items.addAll(entry.getValue());
                if (LocationExist(locName, locationId)) {
                    continue;
                }
                Location location = new Location(locationId, locName);
                addLocation(location);
            }
            addItems(items);
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

         List<Item> items = new ArrayList<>();

        for (Map.Entry<String, List<Item>> entry: locationItems.entrySet()) {
            updateItems(entry.getValue());
            String[] locationId_Name = entry.getKey().split(",");
            if (locationId_Name.length != 2) {
                continue;
            }
            Location location = getLocation(locationId_Name[0]);
            if (location == null) continue;

            location.Name = locationId_Name[1];
            updateLocation(location);
            items.addAll(entry.getValue());
        }

        updateItems(items);
    }

    private boolean hashMapKeyFailCheck(String key) {
        if (key == null || key.isEmpty()) return true;
        if (key.split(",").length != 2) return true;
        return false;
    }



}
