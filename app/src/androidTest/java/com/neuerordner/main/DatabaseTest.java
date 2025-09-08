package com.neuerordner.main;

import static org.junit.Assert.fail;

import android.content.Context;

import com.neuerordner.main.data.DatabaseService;
import com.neuerordner.main.data.Item;
import com.neuerordner.main.data.Location;
import com.github.javafaker.Faker;

import org.junit.After;
import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import androidx.test.platform.app.InstrumentationRegistry;

public class DatabaseTest {

    private DatabaseService _dbService;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private Random random = new Random();


    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();


        _dbService = new DatabaseService(context);
    }

    @After
    public void tearDown() {
        _dbService.close();
    }

    public void insertion(int locationSize, int itemSize) {
        Map<String, List<Item>> locationItems = generateMockEntries(locationSize, itemSize);
        try {
            _dbService.InsertHashMap(locationItems);
        } catch (Exception exception) {
            fail("Database throw an exception handling Test: " + exception.toString());
        }
    }

    public String generateRandomString(int length) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            Character c = CHARACTERS.charAt(random.nextInt(CHARACTERS.length()-1));
            builder.append(c);
        }

        return builder.toString();
    }

    public int randomeRange(int max, double deviation) {
        Random rand = new Random();

        int maxDeviationSpread = (int) (100 - (100 * deviation));
        double randomDeviation = (100 - rand.nextInt(maxDeviationSpread)) / 100;
        return (int) (max * randomDeviation);
    }
    public Map<String, List<Item>> generateMockEntries(int locations, int items, double deviation) {
        Random rand = new Random();

        int maxDeviationSpread = (int) (100 - (100 * deviation));
        double randomDevationLocation = (100 - rand.nextInt(maxDeviationSpread)) / 100;
        double randomDevationItems = (100 - rand.nextInt(maxDeviationSpread)) / 100;

        int randomizedLocation = (int) (locations * randomDevationLocation);
        int randomizedItems = (int) (items * randomDevationItems);

        return generateMockEntries(randomizedLocation, randomizedItems);
    }
    public Map<String, List<Item>> generateMockEntries(int locations, int items) {
        Faker faker = new Faker();
        Random random = new Random();
        Map<String, List<Item>> locationItems = new HashMap<>();

        for(var i = 0; i < locations; i++) {
            String locationName = generateRandomString(5 + random.nextInt(15));
            Location location = new Location(UUID.randomUUID().toString(), locationName, OffsetDateTime.now());
            List<Item> itemList = new ArrayList<Item>();

            for (var j = 0; j < items; j++) {
                //Randome Name
                String itemName = generateRandomString(10 + random.nextInt(39));
                //Generate Randome Date in the Range of 2021 12 12 -> Now
                Date from = new Date(1640362931);
                Date to = new Date(1753460531);
                OffsetDateTime time = faker.date().between(from, to).toInstant().atOffset(ZoneOffset.UTC);
                //Generate Random integer in the range 1 -> 1000001
                int quantity = faker.number().numberBetween(1, 1000);
                Item item = new Item(UUID.randomUUID().toString(), location.Id, itemName, quantity, time);
                itemList.add(item);
            }

            locationItems.put(locationName, itemList);
        }
        return locationItems;
    }


    @Test
    public void testInsertEmptyMap() {
        try {
            _dbService.InsertHashMap(new HashMap<>());
        } catch (Exception e) {
            fail("InsertHashMap sollte bei leerer Map keine Exception werfen");
        }
        Assert.assertEquals(0, _dbService.getAllItems().size());
        Assert.assertEquals(0, _dbService.getAllLocations().size());
    }

    @Test
    public void testInsertThousandEntries() {
        int total_locations = 1000;
        int entries_each = 100;
        int total_items = total_locations * entries_each;
        Map<String, List<Item>> locationItemsMap = generateMockEntries(total_locations, entries_each);
        try {
            _dbService.InsertHashMap(locationItemsMap);
        } catch (Exception e) {
            fail("Error Inserting Big Map");
        }

        int locationSize = _dbService.getAllLocations().size();
        int itemSize = _dbService.getAllItems().size();

        Assert.assertEquals(total_locations, locationSize);
        Assert.assertEquals(total_items, itemSize);
    }

    @Test
    public void testInsertDuplicateLocationIgnored() {
        // Zwei verschiedene Items unter derselben Location
        String locationName = "DuplicateLocation";
        Faker faker = new Faker();

        Item item1 = new Item(UUID.randomUUID().toString(), "", faker.food().ingredient(), 3, OffsetDateTime.now());
        Item item2 = new Item(UUID.randomUUID().toString(), "", faker.food().ingredient(), 5, OffsetDateTime.now());

        Map<String, List<Item>> locationItems = new HashMap<>();
        locationItems.put(locationName, List.of(item1));

        try {
            _dbService.InsertHashMap(locationItems);
        } catch (Exception e) {
            fail("Fehler beim ersten Insert");
        }

        // Versuch, dieselbe Location erneut einzufügen (soll übersprungen werden)
        locationItems.put(locationName, List.of(item2));
        try {
            _dbService.InsertHashMap(locationItems);
        } catch (Exception e) {
            fail("Fehler beim zweiten Insert");
        }

        Assert.assertEquals(1, _dbService.getAllLocations().size());
        Assert.assertEquals(1, _dbService.getAllItems().size());
    }

    @Test
    public void testUpdateExistingItem() {
        Faker faker = new Faker();

        String locationName = faker.funnyName().name();
        String itemId = UUID.randomUUID().toString();
        String itemName = faker.food().ingredient();
        int initialQuantity = 10;
        int updatedQuantity = 25;

        OffsetDateTime time = OffsetDateTime.now();

        Item item = new Item(itemId, "", itemName, initialQuantity, time);
        Map<String, List<Item>> itemLocation = new HashMap<>();
        itemLocation.put(locationName, List.of(item));

        try {
            _dbService.InsertHashMap(itemLocation);
        } catch (Exception e) {
            fail("Insert schlug fehl: " + e.getMessage());
        }

        // Update-Logik testen
        item.Quantity = updatedQuantity;
        try {
            _dbService.UpdateDatabase(itemLocation);
        } catch (Exception e) {
            fail("Update schlug fehl: " + e.getMessage());
        }

        List<Item> items = _dbService.getAllItems();
        Assert.assertEquals(1, items.size());
        Assert.assertEquals(updatedQuantity, items.get(0).Quantity);
    }

    @Test
    public void testEraseAndSetupNew() {
        // 1. Insert some data
        insertion(3, 5);
        Assert.assertTrue(_dbService.getAllItems().size() > 0);
        Assert.assertTrue(_dbService.getAllLocations().size() > 0);

        // 2. Erase everything and insert something new
        Map<String, List<Item>> freshData = generateMockEntries(2, 3);
        try {
            _dbService.EraseAndSetupNew(freshData);
        } catch (Exception e) {
            fail("EraseAndSetupNew warf eine Exception: " + e.getMessage());
        }

        // 3. Nur die neuen Items/Locations dürfen vorhanden sein
        Assert.assertEquals(2, _dbService.getAllLocations().size());
        int totalNewItems = freshData.values().stream().mapToInt(List::size).sum();
        Assert.assertEquals(totalNewItems, _dbService.getAllItems().size());
    }

}
