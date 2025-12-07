package com.example.pokerun.ui.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.pokerun.data.database.entity.PokemonEntity;
import com.example.pokerun.data.repository.PokemonRepository;

import java.util.List;

public class PokemonViewModel extends AndroidViewModel {
    private PokemonRepository pokemonRepository;
    
    public PokemonViewModel(Application application) {
        super(application);
        pokemonRepository = new PokemonRepository(application);
    }
    
    public LiveData<List<PokemonEntity>> getObtainedPokemon() {
        return pokemonRepository.getAllObtainedPokemon();
    }
    
    public LiveData<PokemonEntity> getPokemonById(int id) {
        return pokemonRepository.getPokemonById(id);
    }
}

