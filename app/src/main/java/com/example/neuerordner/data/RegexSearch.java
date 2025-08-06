package com.example.neuerordner.data;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexSearch<E> {
    private AppDatabase db;

    public RegexSearch(AppDatabase db) {
        this.db = db;
    }

    public HashMap<String, String> search(String query, Boolean isLocation) {
        if (isLocation) {
            List<Location> locations = db.locationDAO().getAll();
            return searchFromList(locations, query);

        } else {
            List<Item> items = db.itemDao().getSyncedAll();
            System.out.println("ITEMS: " + items.size());
            return searchFromList(items, query);
        }
    }

    private <E extends NameAccess>HashMap<String, String> searchFromList(List<E> mapList, String query) {
        Pattern pattern = Pattern.compile(query.trim().toLowerCase(), Pattern.CASE_INSENSITIVE);
        HashMap<String, String> matchingList = new HashMap<>();
        for (E entry : mapList) {
            if (entry != null) {
                String name = entry.getname().trim().toLowerCase();
                System.out.println("NAME FROM REGEX CALL: "+name);
                Matcher matcher = pattern.matcher(name);
                if (matcher.find()) {
                    matchingList.put( entry.getid(),name);
                }
            }
        }
        System.out.println("MATCHINGLIST SIZE: " + matchingList.size());
        return matchingList;
    }

}
