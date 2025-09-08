package com.neuerordner.main.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class GlobalViewModel extends ViewModel {
    private  MutableLiveData<ActiveLocation> activeLocation = new MutableLiveData<ActiveLocation>();
    private MutableLiveData<List<Item>> globalItems = new MutableLiveData<List<Item>>();
    private MutableLiveData<String> textMlScanned = new MutableLiveData<>();
    private MutableLiveData<Location> updateLocation = new MutableLiveData<>();

    public LiveData<List<Item>> getGlobalItems() {
        return globalItems;
    }
    public void setGlobalItems(List<Item> items) {
        globalItems.setValue(items);
    }
    public LiveData<ActiveLocation> getActionLocation() {
        return activeLocation;
    }
    public void setActionLocation(ActiveLocation activeLocation) {
        this.activeLocation.setValue(activeLocation);
    }
    public LiveData<String> getTextMlScanned() {return textMlScanned;}
    public void setTextMlScanned(String text) {this.textMlScanned.setValue(text);}

    public void setUpdateLocation(Location location) {
        this.updateLocation.setValue(location);
    }
    public LiveData<Location> getUpdateLocation() {
        return this.updateLocation;
    }


}
