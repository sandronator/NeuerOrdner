package com.example.neuerordner.data;

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
     * @param db Appdabase instance
     **/
    public DatabaseService(AppDatabase db){
        this._db = db;
    }

    public List<Location> getAllLocations() {
        return _db.locationDAO().getAll();
    }
    public List<Item> getAllItems() {
        return _db.itemDao().getSyncedAll();
    }

    public static Map<String, List<Item>> getAllLocationsAndItems(AppDatabase db) {
        List<Location> allLocations = db.locationDAO().getAll();
        List<Item> allItems = db.itemDao().getSyncedAll();

        Map<String, String> idToName = allLocations.stream().collect(Collectors.toMap(loc -> loc.Id, loc -> loc.Name));
        Map<String, List<Item>> locationItemsMap = allItems.stream().filter(item -> idToName.containsKey(item.LocationId))
                .collect(Collectors.groupingBy(item -> idToName.get(item.LocationId)));
        return locationItemsMap;
    }

    private void deleteLocations(List<String> locationIds) {
        locationIds.forEach(locationId -> {
            _db.locationDAO().delete(locationId);
        });
    }

    private void updateLocations(List<Location> locations) {
        for (Location location: locations
             ) {
            _db.locationDAO().update(location);
        }
    }

    private void addLocations(List<Location> locations) {
        for (Location location: locations
        ) {
            _db.locationDAO().insert(location);
        }
    }

    private void deleteItems(List<String> itemIds) {
        itemIds.forEach(itemId -> {
            _db.itemDao().delete(itemId);
        });
    }

    private void updateItems(List<Item> items) {
        items.forEach(item -> {
            _db.itemDao().update(item);
        });
    }

    private void addItems(List<Item> items) {
        items.forEach(item -> {
            _db.itemDao().insert(item);
        });
    }

    private void InsertLocationItemEntry(String locationName, List<Item> itemList) {
        Location location = new Location(UUID.randomUUID().toString(), locationName);
        _db.locationDAO().insert(location);

        itemList.forEach(item -> {
            item.LocationId = location.Id;
            _db.itemDao().insert(item);
        });
    }
    public void EraseAndSetupNew(Map<String, List<Item>> locationItems) throws Exception {
        _db.locationDAO().getAll().forEach(location -> {
            _db.locationDAO().delete(location);
        });

        _db.itemDao().getSyncedAll().forEach(item -> {
            _db.itemDao().delete(item);
        });

        InsertIntoDatabase(locationItems);
    }

    public void UpdateAndInsert(Map<String, List<Item>> locationItems) throws Exception {
        UpdateDatabase(locationItems);
        InsertIntoDatabase(locationItems);
    }

    public void InsertIntoDatabase(Map<String, List<Item>> locationItems) throws Exception {
        if (locationItems.isEmpty()) {
            return;
        }
        Set<String> locationNamesInDbSet = getAllLocations().stream().map(location -> location.Name).collect(Collectors.toSet());


        if (locationNamesInDbSet.isEmpty()) {
            locationItems.forEach((locationName, itemList) -> {
                InsertLocationItemEntry(locationName, itemList);
            });
        } else {
            // Redundanz Check
            locationItems.forEach((locationName, itemList) -> {
                if (!locationNamesInDbSet.contains(locationName)) {
                    InsertLocationItemEntry(locationName, itemList);
                }
            });
        }

    }

    public void UpdateDatabase(Map<String, List<Item>> locationItems) throws Exception {
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
