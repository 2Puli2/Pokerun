package com.example.pokerun.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.pokerun.data.database.entity.UserSettingsEntity;

@Dao
public interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1")
    LiveData<UserSettingsEntity> getSettings();
    
    @Query("SELECT * FROM user_settings WHERE id = 1")
    UserSettingsEntity getSettingsSync();
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserSettingsEntity settings);
    
    @Update
    void update(UserSettingsEntity settings);
    
    @Query("UPDATE user_settings SET language = :language WHERE id = 1")
    void updateLanguage(String language);
    
    @Query("UPDATE user_settings SET distanceUnit = :unit WHERE id = 1")
    void updateDistanceUnit(String unit);
}






