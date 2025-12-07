package com.example.pokerun.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.pokerun.data.database.entity.PokemonEntity;

import java.util.List;

@Dao
public interface PokemonDao {
    @Query("SELECT * FROM pokemon WHERE isObtained = 1 ORDER BY pokedexNumber")
    LiveData<List<PokemonEntity>> getAllObtainedPokemon();
    
    @Query("SELECT * FROM pokemon WHERE id = :id")
    LiveData<PokemonEntity> getPokemonById(int id);
    
    @Query("SELECT * FROM pokemon WHERE pokedexNumber = :pokedexNumber")
    PokemonEntity getPokemonByPokedexNumber(int pokedexNumber);
    
    @Query("SELECT * FROM pokemon WHERE isObtained = 0")
    List<PokemonEntity> getUnobtainedPokemon();
    
    @Query("SELECT COUNT(*) FROM pokemon WHERE isObtained = 1")
    LiveData<Integer> getObtainedCount();
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(PokemonEntity pokemon);
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<PokemonEntity> pokemonList);
    
    @Query("SELECT COUNT(*) FROM pokemon")
    int getCount();
    
    @Update
    void update(PokemonEntity pokemon);
    
    @Query("UPDATE pokemon SET isObtained = 1, obtainedDate = :date WHERE id = :id")
    void markAsObtained(int id, long date);
    
    @Query("SELECT * FROM pokemon WHERE id = :id")
    PokemonEntity getPokemonByIdSync(int id);
    
    @Query("UPDATE pokemon SET pokedexNumber = :newPokedexNumber, name = :newName, type1 = :newType1, type2 = :newType2, evolutionStage = :newStage, evolvesFrom = :newEvolvesFrom, evolvesTo = :newEvolvesTo WHERE id = :pokemonId")
    void updatePokemonEvolution(int pokemonId, int newPokedexNumber, String newName, String newType1, String newType2, int newStage, int newEvolvesFrom, int newEvolvesTo);
}

