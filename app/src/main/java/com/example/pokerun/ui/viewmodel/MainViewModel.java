package com.example.pokerun.ui.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.pokerun.data.database.entity.BagEntity;
import com.example.pokerun.data.database.entity.PokemonEntity;
import com.example.pokerun.data.database.entity.UserSettingsEntity;
import com.example.pokerun.data.repository.BagRepository;
import com.example.pokerun.data.repository.PokemonRepository;
import com.example.pokerun.data.repository.UserSettingsRepository;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private PokemonRepository pokemonRepository;
    private BagRepository bagRepository;
    private UserSettingsRepository settingsRepository;
    
    public MainViewModel(Application application) {
        super(application);
        pokemonRepository = new PokemonRepository(application);
        bagRepository = new BagRepository(application);
        settingsRepository = new UserSettingsRepository(application);
        
        // Los datos ya se inicializan en PokeRunApplication
        // No es necesario inicializarlos aquí de nuevo
    }
    
    public LiveData<List<PokemonEntity>> getObtainedPokemon() {
        return pokemonRepository.getAllObtainedPokemon();
    }
    
    public LiveData<BagEntity> getBag() {
        return bagRepository.getBag();
    }
    
    public LiveData<UserSettingsEntity> getSettings() {
        return settingsRepository.getSettings();
    }
}

