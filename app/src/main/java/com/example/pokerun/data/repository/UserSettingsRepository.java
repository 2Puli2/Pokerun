package com.example.pokerun.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.pokerun.data.database.PokeRunDatabase;
import com.example.pokerun.data.database.dao.UserSettingsDao;
import com.example.pokerun.data.database.entity.UserSettingsEntity;

public class UserSettingsRepository {
    private UserSettingsDao userSettingsDao;
    
    public UserSettingsRepository(Context context) {
        PokeRunDatabase database = PokeRunDatabase.getDatabase(context);
        this.userSettingsDao = database.userSettingsDao();
        initializeSettings();
    }
    
    public LiveData<UserSettingsEntity> getSettings() {
        return userSettingsDao.getSettings();
    }
    
    public UserSettingsEntity getSettingsSync() {
        return userSettingsDao.getSettingsSync();
    }
    
    private void initializeSettings() {
        new Thread(() -> {
            UserSettingsEntity settings = userSettingsDao.getSettingsSync();
            if (settings == null) {
                userSettingsDao.insert(new UserSettingsEntity("es", "km"));
            }
        }).start();
    }
    
    public void updateLanguage(String language) {
        new Thread(() -> {
            userSettingsDao.updateLanguage(language);
        }).start();
    }
    
    public void updateDistanceUnit(String unit) {
        new Thread(() -> {
            userSettingsDao.updateDistanceUnit(unit);
        }).start();
    }
}






