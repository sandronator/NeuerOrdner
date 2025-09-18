package com.neuerordner.main.data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexSearch<E> {
    private RegexSearch() {}

    public static List<? extends NameAccess> search(String query, Boolean isLocation, List<? extends NameAccess> randomList) {
        if (isLocation) {
            return searchFromList(randomList, query);

        } else {
            return searchFromList(randomList, query);
        }
    }

    private static <E extends NameAccess> List<? extends NameAccess> searchFromList(List<E> mapList, String query) {
        Pattern pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
        List<E> matchingList = new ArrayList<>();
        for (E entry : mapList) {
            if (entry != null) {
                String name = entry.getname();
                Matcher matcher = pattern.matcher(name);
                if (matcher.find()) {
                    matchingList.add(entry);
                }
            }
        }
        return matchingList;
    }

}
