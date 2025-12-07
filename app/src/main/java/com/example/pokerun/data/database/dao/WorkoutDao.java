package com.example.pokerun.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.pokerun.data.database.entity.WorkoutEntity;

import java.util.List;

@Dao
public interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY startTime DESC")
    LiveData<List<WorkoutEntity>> getAllWorkouts();
    
    @Query("SELECT * FROM workouts WHERE id = :id")
    LiveData<WorkoutEntity> getWorkoutById(long id);
    
    @Insert
    void insert(WorkoutEntity workout);
    
    @Query("SELECT SUM(distance) FROM workouts")
    LiveData<Double> getTotalDistance();
}






