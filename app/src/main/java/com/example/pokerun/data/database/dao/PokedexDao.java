package com.example.pokerun.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.pokerun.data.database.entity.PokedexEntryEntity;

import java.util.List;

@Dao
public interface PokedexDao {
    @Query("SELECT * FROM pokedex_entries ORDER BY pokedexNumber")
    LiveData<List<PokedexEntryEntity>> getAllEntries();
    
    @Query("SELECT * FROM pokedex_entries WHERE pokedexNumber = :number")
    LiveData<PokedexEntryEntity> getEntryByNumber(int number);
    
    @Query("SELECT * FROM pokedex_entries WHERE isUnlocked = 1 ORDER BY pokedexNumber")
    LiveData<List<PokedexEntryEntity>> getUnlockedEntries();
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(PokedexEntryEntity entry);
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<PokedexEntryEntity> entries);
    
    @Query("SELECT COUNT(*) FROM pokedex_entries")
    int getCount();
    
    @Update
    void update(PokedexEntryEntity entry);
    
    @Query("UPDATE pokedex_entries SET isUnlocked = 1 WHERE pokedexNumber = :number")
    void unlockEntry(int number);
}

