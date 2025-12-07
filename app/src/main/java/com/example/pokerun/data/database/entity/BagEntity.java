package com.example.pokerun.data.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bag")
public class BagEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public int eggs;
    public int rareCandies;
    
    public BagEntity() {
        this.eggs = 0;
        this.rareCandies = 0;
    }
    
    public BagEntity(int eggs, int rareCandies) {
        this.eggs = eggs;
        this.rareCandies = rareCandies;
    }
}






