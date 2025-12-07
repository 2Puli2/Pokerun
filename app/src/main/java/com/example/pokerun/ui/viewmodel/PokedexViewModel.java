package com.example.pokerun.ui.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.pokerun.data.database.entity.PokedexEntryEntity;
import com.example.pokerun.data.repository.PokedexRepository;
import com.example.pokerun.data.repository.UserSettingsRepository;

import java.util.List;

public class PokedexViewModel extends AndroidViewModel {
    private PokedexRepository pokedexRepository;
    private UserSettingsRepository settingsRepository;
    
    public PokedexViewModel(Application application) {
        super(application);
        pokedexRepository = new PokedexRepository(application);
        settingsRepository = new UserSettingsRepository(application);
        // Los datos ya se inicializan en PokeRunApplication
    }
    
    public LiveData<List<PokedexEntryEntity>> getAllEntries() {
        return pokedexRepository.getAllEntries();
    }
    
    public LiveData<PokedexEntryEntity> getEntryByNumber(int number) {
        return pokedexRepository.getEntryByNumber(number);
    }
}

