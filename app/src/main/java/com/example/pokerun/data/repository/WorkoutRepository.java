package com.example.pokerun.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.pokerun.data.database.PokeRunDatabase;
import com.example.pokerun.data.database.dao.WorkoutDao;
import com.example.pokerun.data.database.entity.WorkoutEntity;

import java.util.List;

public class WorkoutRepository {
    private WorkoutDao workoutDao;
    
    public WorkoutRepository(Context context) {
        PokeRunDatabase database = PokeRunDatabase.getDatabase(context);
        this.workoutDao = database.workoutDao();
    }
    
    public LiveData<List<WorkoutEntity>> getAllWorkouts() {
        return workoutDao.getAllWorkouts();
    }
    
    public LiveData<WorkoutEntity> getWorkoutById(long id) {
        return workoutDao.getWorkoutById(id);
    }
    
    public LiveData<Double> getTotalDistance() {
        return workoutDao.getTotalDistance();
    }
    
    public void insertWorkout(WorkoutEntity workout) {
        new Thread(() -> {
            workoutDao.insert(workout);
        }).start();
    }
}






