package com.example.neuerordner.data;

import java.util.List;


@Deprecated
public class ActiveLocationController {
    private static AppDatabase db;

    public ActiveLocationController(AppDatabase db) {
        this.db = db;
    }

    public void insert(ActiveLocation newAl) {
        List<ActiveLocation> activeLocationList = getAll();
        for (ActiveLocation al: activeLocationList) {
            db.activeLocationDao().delete(al);
        }
        db.activeLocationDao().insert(newAl);
    }
    public void delete() {
        List<ActiveLocation> activeLocationList = getAll();
        for (ActiveLocation al : activeLocationList) {
            db.activeLocationDao().delete(al);
        }
    }
    public List<ActiveLocation> getAll() {
         return db.activeLocationDao().getAll();
    }
    public void update(ActiveLocation al) {
        insert(al);
    }


}
