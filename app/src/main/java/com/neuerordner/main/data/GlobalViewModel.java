package com.neuerordner.main.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class GlobalViewModel extends ViewModel {

    //Initalize LiveData
    private final static MutableLiveData<ActiveLocation> activeLocation = new MutableLiveData<ActiveLocation>();
    private final static MutableLiveData<List<Item>> globalItems = new MutableLiveData<List<Item>>();
    private final static MutableLiveData<String> textMlScanned = new MutableLiveData<>();
    private final static MutableLiveData<Location> updateLocation = new MutableLiveData<>();
    private final static MutableLiveData<LocalDate> date = new MutableLiveData<>();
    private final static MutableLiveData<Location> lastClickedLocation = new MutableLiveData<>();

    //Getter
    public LiveData<ActiveLocation> getActionLocation() {
        return activeLocation;
    }
    public LiveData<List<Item>> getGlobalItems() {
        return globalItems;
    }
    public LiveData<String> getTextMlScanned() {return textMlScanned;}
    public LiveData<Location> getUpdateLocation() {
        return this.updateLocation;
    }
    public LiveData<LocalDate> getDate() { return this.date; }
    public LiveData<Location> getLastClickedLocation() { return this.lastClickedLocation; }

    //Setters
    public void setActiveLocation(ActiveLocation activeLocation) {
        this.activeLocation.setValue(activeLocation);
    }
    public void setGlobalItems(List<Item> items) {
        globalItems.setValue(items);
    }
    public void setTextMlScanned(String text) {this.textMlScanned.setValue(text);}
    public void setUpdateLocation(Location location) {
        this.updateLocation.setValue(location);
    }
    public void setDate(LocalDate date) {
        this.date.setValue(date);
    }
    public void setLastClickedLocation(Location location) {
        this.lastClickedLocation.setValue(location);
    }

}
