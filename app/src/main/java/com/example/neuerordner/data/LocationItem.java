package com.example.neuerordner.data;

import java.util.List;
import java.util.Map;

public class LocationItem {
    public Map<Location, List<Item>> data;

    public LocationItem(Map<Location, List<Item>> data) {
        this.data = data;
    }


}
