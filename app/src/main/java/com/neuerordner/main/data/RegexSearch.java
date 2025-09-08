package com.neuerordner.main.data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexSearch<E> {

    private DatabaseService _dbService;

    public RegexSearch(DatabaseService dbService) {
        this._dbService = dbService;
    }

    public List<? extends NameAccess> search(String query, Boolean isLocation) {
        if (isLocation) {
            List<Location> locations = _dbService.getAllLocations();
            return searchFromList(locations, query);

        } else {
            List<? extends NameAccess> items = _dbService.getAllItems();
            return searchFromList(items, query);
        }
    }

    private <E extends NameAccess> List<? extends NameAccess> searchFromList(List<E> mapList, String query) {
        Pattern pattern = Pattern.compile(query.trim().toLowerCase(), Pattern.CASE_INSENSITIVE);
        List<E> matchingList = new ArrayList<>();
        for (E entry : mapList) {
            if (entry != null) {
                String name = entry.getname().trim().toLowerCase();
                Matcher matcher = pattern.matcher(name);
                if (matcher.find()) {
                    matchingList.add(entry);
                }
            }
        }
        return matchingList;
    }

}
