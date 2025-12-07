package com.example.pokerun.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.pokerun.data.database.entity.BagEntity;

@Dao
public interface BagDao {
    @Query("SELECT * FROM bag LIMIT 1")
    LiveData<BagEntity> getBag();
    
    @Query("SELECT * FROM bag LIMIT 1")
    BagEntity getBagSync();
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BagEntity bag);
    
    @Update
    void update(BagEntity bag);
    
    @Query("UPDATE bag SET eggs = eggs + :amount")
    void addEggs(int amount);
    
    @Query("UPDATE bag SET rareCandies = rareCandies + :amount")
    void addCandies(int amount);
    
    @Query("UPDATE bag SET eggs = eggs - 1 WHERE eggs > 0")
    void removeEgg();
    
    @Query("UPDATE bag SET rareCandies = rareCandies - :amount WHERE rareCandies >= :amount")
    void removeCandies(int amount);
}

