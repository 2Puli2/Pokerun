package com.example.pokerun.ui.viewmodel;

import android.app.Application;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pokerun.data.database.entity.WorkoutEntity;
import com.example.pokerun.data.repository.BagRepository;
import com.example.pokerun.data.repository.WorkoutRepository;

import java.util.List;

/**
 * ViewModel para la pantalla de Entrenamiento.
 * 
 * PATRÓN MVVM - CAPA VIEWMODEL:
 * Este ViewModel actúa como intermediario entre la Vista (WorkoutFragment) y
 * el Modelo (WorkoutRepository, BagRepository). Sus responsabilidades incluyen:
 * 
 * 1. GESTIÓN DEL ESTADO:
 *    - Mantiene el estado del entrenamiento activo mediante LiveData
 *    - Expone datos observables (steps, distance, isWorkoutActive, startTime)
 *    - Sobrevive a cambios de configuración gracias a AndroidViewModel
 * 
 * 2. LÓGICA DE NEGOCIO:
 *    - Calcula la distancia basándose en pasos (1 paso ≈ 0.7 metros)
 *    - Implementa el sistema de recompensas de la aplicación
 *    - Coordina la persistencia de entrenamientos
 * 
 * 3. GESTIÓN DE HARDWARE:
 *    - Encapsula la lectura del sensor TYPE_STEP_COUNTER
 *    - Gestiona el ciclo de vida del sensor (registro/desregistro)
 * 
 * SISTEMA DE RECOMPENSAS:
 * - 1 huevo Pokémon por entrenamiento que supere 5km
 * - 1 caramelo raro por cada 5km recorridos (15km = 3 caramelos)
 * 
 * @see WorkoutFragment Vista que observa este ViewModel
 * @see WorkoutRepository Repositorio para persistir entrenamientos
 * @see BagRepository Repositorio para actualizar el inventario
 */
public class WorkoutViewModel extends AndroidViewModel {
    
    // Constante: metros aproximados por paso
    private static final double METERS_PER_STEP = 0.7;
    
    // Repositorios
    private final WorkoutRepository workoutRepository;
    private final BagRepository bagRepository;
    
    // Estado del entrenamiento
    private final MutableLiveData<Boolean> isWorkoutActive = new MutableLiveData<>(false);
    private final MutableLiveData<Long> startTime = new MutableLiveData<>(0L);
    private final MutableLiveData<Integer> steps = new MutableLiveData<>(0);
    private final MutableLiveData<Double> distance = new MutableLiveData<>(0.0);
    
    // Sensor de pasos
    private final SensorManager sensorManager;
    private final Sensor stepCounterSensor;
    private int initialSteps = 0;
    private boolean hasInitialSteps = false;
    
    public WorkoutViewModel(Application application) {
        super(application);
        workoutRepository = new WorkoutRepository(application);
        bagRepository = new BagRepository(application);
        
        // Inicializar sensor de pasos
        sensorManager = (SensorManager) application.getSystemService(android.content.Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager != null ? 
            sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) : null;
    }
    
    // ==================== GETTERS LIVEDATA ====================
    
    public LiveData<List<WorkoutEntity>> getAllWorkouts() {
        return workoutRepository.getAllWorkouts();
    }
    
    public LiveData<Boolean> getIsWorkoutActive() {
        return isWorkoutActive;
    }
    
    public LiveData<Long> getStartTime() {
        return startTime;
    }
    
    public LiveData<Integer> getSteps() {
        return steps;
    }
    
    public LiveData<Double> getDistance() {
        return distance;
    }
    
    /**
     * @return true si el dispositivo tiene sensor de pasos
     */
    public boolean hasStepCounter() {
        return stepCounterSensor != null;
    }
    
    // ==================== CONTROL DEL ENTRENAMIENTO ====================
    
    /**
     * Inicia un nuevo entrenamiento.
     * Activa el sensor de pasos y resetea los contadores.
     */
    public void startWorkout() {
        isWorkoutActive.setValue(true);
        startTime.setValue(System.currentTimeMillis());
        steps.setValue(0);
        distance.setValue(0.0);
        hasInitialSteps = false;
        
        if (stepCounterSensor != null) {
            sensorManager.registerListener(stepListener, stepCounterSensor, 
                SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    
    /**
     * Pausa el entrenamiento actual.
     * Desactiva el sensor de pasos pero mantiene los datos.
     */
    public void stopWorkout() {
        isWorkoutActive.setValue(false);
        
        if (stepCounterSensor != null) {
            sensorManager.unregisterListener(stepListener);
        }
        
        // Actualizar distancia final
        Integer currentSteps = steps.getValue();
        if (currentSteps != null && currentSteps > 0) {
            double distanceKm = (currentSteps * METERS_PER_STEP) / 1000.0;
            distance.setValue(distanceKm);
        }
    }
    
    /**
     * Finaliza el entrenamiento y guarda los resultados.
     * Calcula y otorga las recompensas correspondientes.
     */
    public void finishWorkout(boolean fromStrava, double manualDistance, boolean useManualDistance) {
        stopWorkout();
        
        Long start = startTime.getValue();
        Integer currentSteps = steps.getValue();
        Double currentDistance = distance.getValue();
        
        if (start == null || start == 0) return;
        
        // Determinar distancia final
        double finalDistance;
        if (fromStrava || useManualDistance) {
            finalDistance = manualDistance;
        } else {
            finalDistance = currentDistance != null ? currentDistance : 0.0;
        }
        
        int finalSteps = currentSteps != null ? currentSteps : 0;
        
        // Calcular recompensas
        int eggsEarned = finalDistance >= 5.0 ? 1 : 0;
        int candiesEarned = (int) (finalDistance / 5.0);
        
        // Guardar en base de datos
        saveWorkout(start, finalDistance, finalSteps, fromStrava, eggsEarned, candiesEarned);
    }
    
    /**
     * Guarda un entrenamiento manual (sin usar sensor de pasos).
     */
    public void finishManualWorkout(double distanceKm, long manualStartTime) {
        // Calcular recompensas
        int eggsEarned = distanceKm >= 5.0 ? 1 : 0;
        int candiesEarned = (int) (distanceKm / 5.0);
        
        saveWorkout(manualStartTime, distanceKm, 0, false, eggsEarned, candiesEarned);
    }
    
    /**
     * Guarda el entrenamiento en la base de datos y otorga recompensas
     */
    private void saveWorkout(long start, double distance, int steps, 
                             boolean fromStrava, int eggs, int candies) {
        new Thread(() -> {
            WorkoutEntity workout = new WorkoutEntity(
                start,
                System.currentTimeMillis(),
                distance,
                steps,
                fromStrava,
                eggs,
                candies
            );
            
            workoutRepository.insertWorkout(workout);
            
            // Otorgar recompensas
            if (eggs > 0) {
                bagRepository.addEggsSync(eggs);
            }
            if (candies > 0) {
                bagRepository.addCandiesSync(candies);
            }
        }).start();
    }
    
    // ==================== SENSOR LISTENER ====================
    
    private final SensorEventListener stepListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER) return;
            
            int totalSteps = (int) event.values[0];
            
            // Guardar el valor inicial de pasos al comenzar
            if (!hasInitialSteps) {
                initialSteps = totalSteps;
                hasInitialSteps = true;
            }
            
            // Calcular pasos del entrenamiento actual
            int workoutSteps = totalSteps - initialSteps;
            steps.postValue(workoutSteps);
            
            // Calcular distancia aproximada
            double distanceKm = (workoutSteps * METERS_PER_STEP) / 1000.0;
            distance.postValue(distanceKm);
        }
        
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // No se necesita implementación
        }
    };
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Asegurar que el sensor se desregistra cuando el ViewModel se destruye
        if (stepCounterSensor != null && sensorManager != null) {
            sensorManager.unregisterListener(stepListener);
        }
    }
}
