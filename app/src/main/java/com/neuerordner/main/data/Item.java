package com.neuerordner.main.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;

@Entity(
        foreignKeys = @ForeignKey(

                entity = Location.class,
                childColumns = "LocationId",
                parentColumns = "Id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value="LocationId")



)
public class Item implements NameAccess, Serializable {
    @PrimaryKey
    @NonNull

    public String Id;
    public String LocationId;
    public String Name;
    public int Quantity;
    public OffsetDateTime Time;
    public LocalDate bestTillDate;

    public Item(String Id, String LocationId, String Name, int Quantity, OffsetDateTime Time, LocalDate bestTillDate) {
        this.Id = Id;
        this.LocationId = LocationId;
        this.Name = Name;
        this.Quantity = Quantity;
        this.Time = Time;
        this.bestTillDate = bestTillDate;
    }

    @Override
    public String getname() {
        return this.Name;
    }

    @Override
    public String getid() {
        return this.Id;
    }

    @Override
    public String getLocationId() {
        return this.LocationId;
    }
    public void setLocationId(String id) {
        this.LocationId = id;
    }

    public int getQuantity() {
        return this.Quantity;
    }
    public void setQuantity(int quantity) {
        this.Quantity = quantity;
    }

    public OffsetDateTime getOffsetDateTime() {
        return this.Time;
    }

    public void setOffsetDateTime(String time) {
        try {
            this.Time = OffsetDateTime.parse(time);
        } catch (DateTimeParseException e) {
            System.out.println("Error parsing String");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Item other = (Item) obj;
        return Id.equals(other.Id);
    }

    @Override
    public int hashCode() {
        return Id != null ? Id.hashCode() : 0;
    }

}
