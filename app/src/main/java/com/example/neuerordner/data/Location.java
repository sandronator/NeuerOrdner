
package com.example.neuerordner.data;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;

@Entity(
        indices = {@Index(value = {"Name"}, unique = true)}
)
public class Location implements NameAccess, Comparable<Location>, Parcelable {
    @PrimaryKey
    @NonNull
    public String Id;
    @NonNull

    public String Name;
    @ColumnInfo(defaultValue = "")
    @NonNull
    public OffsetDateTime CreationDate;

    public Location(String id, String name, OffsetDateTime creationDate) {
        this.Id = id;
        this.Name = name;
        this.CreationDate = creationDate;
    }

    public Location() {
    }

    protected Location(Parcel in) {
        Id = in.readString();
        Name = in.readString();
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

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
        return getid();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null|| getClass() != obj.getClass()) return false;

        Location other = (Location) obj;

        return Id.equals(other.Id);
    }

    public OffsetDateTime getOffsetDateTime() {
        return this.CreationDate;
    }

    public boolean equalsName(Object obj) {
        if (this == obj) return true;
        if (obj == null|| getClass() != obj.getClass()) return false;

        Location other = (Location) obj;
        return Name.equals(other.Name);
    }

    @Override
    public int compareTo(Location o) {
        return this.Name.compareTo(o.Name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.Id);
        dest.writeString(this.Name);
        dest.writeString(DateTimeConverter.DateTimeToString(this.CreationDate));
    }
}
