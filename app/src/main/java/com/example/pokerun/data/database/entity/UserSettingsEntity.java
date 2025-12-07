package com.example.pokerun.data.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_settings")
public class UserSettingsEntity {
    @PrimaryKey
    public int id = 1; // Solo una fila
    
    public String language; // "es" o "en"
    public String distanceUnit; // "km" o "mi"
    
    public UserSettingsEntity() {
        this.language = "es";
        this.distanceUnit = "km";
    }
    
    public UserSettingsEntity(String language, String distanceUnit) {
        this.language = language;
        this.distanceUnit = distanceUnit;
    }
}






