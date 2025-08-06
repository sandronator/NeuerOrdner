
package com.example.neuerordner.data;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        indices = {@Index(value = {"Name"}, unique = true)}
)
public class Location implements NameAccess{
    @PrimaryKey
    @NonNull
    public String Id;

    public String Name;

    public Location(String id, String name) {
        this.Id = id;
        this.Name = name;
    }

    public Location() {
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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null|| getClass() != obj.getClass()) return false;

        Location other = (Location) obj;

        return Id.equals(other.Id);
    }
    public boolean equalsName(Object obj) {
        if (this == obj) return true;
        if (obj == null|| getClass() != obj.getClass()) return false;

        Location other = (Location) obj;
        return Name.equals(other.Name);
    }
}
