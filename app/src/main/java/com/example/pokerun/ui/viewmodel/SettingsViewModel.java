package com.example.pokerun.ui.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.pokerun.data.database.entity.UserSettingsEntity;
import com.example.pokerun.data.repository.UserSettingsRepository;

public class SettingsViewModel extends AndroidViewModel {
    private UserSettingsRepository settingsRepository;
    
    public SettingsViewModel(Application application) {
        super(application);
        settingsRepository = new UserSettingsRepository(application);
    }
    
    public LiveData<UserSettingsEntity> getSettings() {
        return settingsRepository.getSettings();
    }
    
    public void updateLanguage(String language) {
        settingsRepository.updateLanguage(language);
    }
    
    public void updateDistanceUnit(String unit) {
        settingsRepository.updateDistanceUnit(unit);
    }
}






