package com.example.neuerordner.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(
    tableName = "activelocation"

)
public class ActiveLocation {
    @PrimaryKey
    @NonNull
    public String Id;
    public String Name;
}
