package com.example.pokerun.ui.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.pokerun.data.database.entity.BagEntity;
import com.example.pokerun.data.repository.BagRepository;
import com.example.pokerun.data.repository.PokedexRepository;
import com.example.pokerun.data.repository.PokemonRepository;

/**
 * ViewModel para la pantalla de Mochila.
 * Gestiona los items del jugador (huevos y caramelos raros) y las operaciones relacionadas.
 */
public class BagViewModel extends AndroidViewModel {
    private static final String TAG = "BagViewModel";
    
    private final BagRepository bagRepository;
    private final PokemonRepository pokemonRepository;
    private final PokedexRepository pokedexRepository;
    
    public BagViewModel(Application application) {
        super(application);
        bagRepository = new BagRepository(application);
        pokemonRepository = new PokemonRepository(application);
        pokedexRepository = new PokedexRepository(application);
    }
    
    /**
     * Obtiene el contenido de la mochila como LiveData
     * @return LiveData con la entidad de la mochila
     */
    public LiveData<BagEntity> getBag() {
        return bagRepository.getBag();
    }
    
    /**
     * Marca un Pokémon como obtenido y lo desbloquea en la Pokédex
     * @param pokemonId ID del Pokémon obtenido
     * @param pokedexNumber Número de Pokédex para desbloquear
     */
    public void markPokemonObtained(int pokemonId, int pokedexNumber) {
        pokemonRepository.markPokemonAsObtained(pokemonId);
        pokedexRepository.unlockEntry(pokedexNumber);
    }
}
