package com.example.pokerun.data.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workouts")
public class WorkoutEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public long startTime;
    public long endTime;
    public double distance; // en kilómetros
    public int steps;
    public boolean isFromStrava;
    public int eggsEarned;
    public int candiesEarned;
    
    public WorkoutEntity() {}
    
    public WorkoutEntity(long startTime, long endTime, double distance, int steps, 
                        boolean isFromStrava, int eggsEarned, int candiesEarned) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.distance = distance;
        this.steps = steps;
        this.isFromStrava = isFromStrava;
        this.eggsEarned = eggsEarned;
        this.candiesEarned = candiesEarned;
    }
}






