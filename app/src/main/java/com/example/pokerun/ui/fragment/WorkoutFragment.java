package com.example.pokerun.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pokerun.R;
import com.example.pokerun.data.database.entity.UserSettingsEntity;
import com.example.pokerun.data.repository.UserSettingsRepository;
import com.example.pokerun.ui.viewmodel.WorkoutViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Fragment para registrar entrenamientos de running.
 * 
 * PATRÓN MVVM - CAPA VIEW:
 * Este Fragment representa la Vista en el patrón MVVM. Su responsabilidad
 * se limita a:
 * - Observar los LiveData expuestos por WorkoutViewModel
 * - Actualizar la UI cuando los datos cambian
 * - Delegar las acciones del usuario al ViewModel
 * 
 * El Fragment NO contiene lógica de negocio. Toda la gestión del sensor
 * de pasos, cálculo de distancia y sistema de recompensas se delega al
 * WorkoutViewModel, lo que permite:
 * - Testear la lógica sin instrumentación Android
 * - Sobrevivir a cambios de configuración (rotaciones)
 * - Mantener la Vista lo más simple posible
 * 
 * FUNCIONALIDADES:
 * - Entrenamiento con sensor TYPE_STEP_COUNTER del dispositivo
 * - Entrada manual de distancia (para dispositivos sin sensor)
 * - Temporizador en tiempo real
 * - Sistema de recompensas: 1 huevo por entrenamiento >5km, 
 *   1 caramelo raro por cada 5km recorridos
 * 
 * @see WorkoutViewModel ViewModel que gestiona la lógica de entrenamientos
 * @see WorkoutRepository Repositorio que persiste los entrenamientos
 */
public class WorkoutFragment extends Fragment {
    
    // ViewModel y Repositorio
    private WorkoutViewModel viewModel;
    private UserSettingsRepository settingsRepository;
    
    // Vistas - Entrenamiento con sensor
    private TextView tvDistance;
    private TextView tvSteps;
    private TextView tvTime;
    private MaterialButton btnStart;
    private MaterialButton btnStop;
    private MaterialButton btnFinish;
    
    // Vistas - Entrada manual
    private TextInputEditText etManualDistance;
    private TextInputLayout tilManualDistance;
    private MaterialButton btnSaveManual;
    
    // Control del temporizador
    private Handler handler;
    private Runnable timeRunnable;
    private long startTime = 0;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeComponents(view);
        setupListeners();
        setupObservers();
        updateDistanceUnitHint();
    }
    
    /**
     * Inicializa todos los componentes del fragment
     */
    private void initializeComponents(@NonNull View view) {
        viewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);
        settingsRepository = new UserSettingsRepository(requireContext());
        handler = new Handler(Looper.getMainLooper());
        
        // Vistas de entrenamiento con sensor
        tvDistance = view.findViewById(R.id.tv_distance);
        tvSteps = view.findViewById(R.id.tv_steps);
        tvTime = view.findViewById(R.id.tv_time);
        btnStart = view.findViewById(R.id.btn_start_workout);
        btnStop = view.findViewById(R.id.btn_stop_workout);
        btnFinish = view.findViewById(R.id.btn_finish_workout);
        
        // Vistas de entrada manual
        etManualDistance = view.findViewById(R.id.et_manual_distance);
        tilManualDistance = view.findViewById(R.id.til_manual_distance);
        btnSaveManual = view.findViewById(R.id.btn_save_manual_workout);
    }
    
    /**
     * Configura los listeners de los botones
     */
    private void setupListeners() {
        btnStart.setOnClickListener(v -> onStartWorkoutClicked());
        btnStop.setOnClickListener(v -> onStopWorkoutClicked());
        btnFinish.setOnClickListener(v -> onFinishWorkoutClicked());
        btnSaveManual.setOnClickListener(v -> onSaveManualWorkoutClicked());
    }
    
    /**
     * Configura los observadores de LiveData
     */
    private void setupObservers() {
        // Observar distancia
        viewModel.getDistance().observe(getViewLifecycleOwner(), distance -> {
            if (distance != null && distance > 0) {
                tvDistance.setVisibility(View.VISIBLE);
                updateDistanceDisplay(distance);
            }
        });
        
        // Observar pasos
        viewModel.getSteps().observe(getViewLifecycleOwner(), steps -> {
            if (steps != null && steps > 0) {
                tvSteps.setVisibility(View.VISIBLE);
                tvSteps.setText(getString(R.string.steps) + ": " + steps);
            }
        });
        
        // Observar estado del entrenamiento
        viewModel.getIsWorkoutActive().observe(getViewLifecycleOwner(), isActive -> {
            if (isActive != null) {
                updateUIForWorkoutState(isActive);
            }
        });
        
        // Observar tiempo de inicio
        viewModel.getStartTime().observe(getViewLifecycleOwner(), time -> {
            if (time != null && time > 0) {
                startTime = time;
                startTimeCounter();
            }
        });
    }
    
    // ==================== ENTRENAMIENTO CON SENSOR ====================
    
    /**
     * Maneja el clic en el botón de iniciar entrenamiento
     */
    private void onStartWorkoutClicked() {
        if (!viewModel.hasStepCounter()) {
            Toast.makeText(getContext(), getString(R.string.no_step_sensor), Toast.LENGTH_LONG).show();
            return;
        }
        viewModel.startWorkout();
    }
    
    /**
     * Maneja el clic en el botón de pausar entrenamiento
     */
    private void onStopWorkoutClicked() {
        viewModel.stopWorkout();
        stopTimeCounter();
    }
    
    /**
     * Maneja el clic en el botón de finalizar entrenamiento
     */
    private void onFinishWorkoutClicked() {
        Double distance = viewModel.getDistance().getValue();
        double distanceKm = distance != null ? distance : 0.0;
        
        viewModel.finishWorkout(false, 0.0, false);
        stopTimeCounter();
        resetUI();
        
        showWorkoutRewards(distanceKm);
    }
    
    // ==================== ENTRADA MANUAL ====================
    
    /**
     * Maneja el clic en el botón de guardar entrenamiento manual
     */
    private void onSaveManualWorkoutClicked() {
        String distanceText = etManualDistance.getText() != null ? 
            etManualDistance.getText().toString() : "";
        
        if (TextUtils.isEmpty(distanceText)) {
            Toast.makeText(getContext(), getString(R.string.enter_valid_distance), Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            double distance = Double.parseDouble(distanceText);
            if (distance <= 0) {
                Toast.makeText(getContext(), getString(R.string.enter_valid_distance), Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Convertir a km si está en millas
            double distanceKm = convertToKilometers(distance);
            
            // Guardar entrenamiento
            long manualStartTime = System.currentTimeMillis() - (60 * 60 * 1000);
            viewModel.finishManualWorkout(distanceKm, manualStartTime);
            
            // Limpiar campo y mostrar recompensas
            etManualDistance.setText("");
            showWorkoutRewards(distanceKm);
            
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), getString(R.string.enter_valid_distance), Toast.LENGTH_SHORT).show();
        }
    }
    
    // ==================== UTILIDADES ====================
    
    /**
     * Convierte la distancia a kilómetros según la unidad configurada
     */
    private double convertToKilometers(double distance) {
        UserSettingsEntity settings = settingsRepository.getSettingsSync();
        String unit = settings != null ? settings.distanceUnit : "km";
        
        if ("mi".equals(unit)) {
            return distance / 0.621371; // Millas a kilómetros
        }
        return distance;
    }
    
    /**
     * Actualiza el hint del campo de distancia según la unidad configurada
     */
    private void updateDistanceUnitHint() {
        UserSettingsEntity settings = settingsRepository.getSettingsSync();
        String unit = settings != null ? settings.distanceUnit : "km";
        
        String suffix = "mi".equals(unit) ? getString(R.string.mi) : getString(R.string.km);
        tilManualDistance.setSuffixText(suffix);
    }
    
    /**
     * Actualiza la visualización de la distancia según la unidad configurada
     */
    private void updateDistanceDisplay(double distanceKm) {
        UserSettingsEntity settings = settingsRepository.getSettingsSync();
        String unit = settings != null ? settings.distanceUnit : "km";
        
        double displayDistance = distanceKm;
        String unitLabel = getString(R.string.km);
        
        if ("mi".equals(unit)) {
            displayDistance = distanceKm * 0.621371;
            unitLabel = getString(R.string.mi);
        }
        
        tvDistance.setText(String.format(Locale.getDefault(), "%.2f %s", displayDistance, unitLabel));
    }
    
    /**
     * Muestra las recompensas obtenidas por el entrenamiento
     */
    private void showWorkoutRewards(double distanceKm) {
        int eggs = distanceKm >= 5.0 ? 1 : 0;
        int candies = (int) (distanceKm / 5.0);
        
        String message;
        if (eggs > 0 || candies > 0) {
            message = getString(R.string.workout_saved) + "\n" + 
                      getString(R.string.workout_rewards, eggs, candies);
        } else {
            message = getString(R.string.workout_saved);
        }
        
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Actualiza la UI según el estado del entrenamiento
     */
    private void updateUIForWorkoutState(boolean isActive) {
        btnStart.setVisibility(isActive ? View.GONE : View.VISIBLE);
        btnStop.setVisibility(isActive ? View.VISIBLE : View.GONE);
        btnFinish.setVisibility(isActive ? View.VISIBLE : View.GONE);
        
        if (isActive) {
            tvDistance.setVisibility(View.VISIBLE);
            tvSteps.setVisibility(View.VISIBLE);
            tvTime.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Inicia el contador de tiempo
     */
    private void startTimeCounter() {
        tvTime.setVisibility(View.VISIBLE);
        
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                if (startTime > 0) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    long hours = TimeUnit.MILLISECONDS.toHours(elapsed);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) % 60;
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60;
                    
                    String timeString = String.format(Locale.getDefault(), 
                        "%02d:%02d:%02d", hours, minutes, seconds);
                    tvTime.setText(getString(R.string.time) + ": " + timeString);
                    
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(timeRunnable);
    }
    
    /**
     * Detiene el contador de tiempo
     */
    private void stopTimeCounter() {
        if (timeRunnable != null) {
            handler.removeCallbacks(timeRunnable);
        }
        startTime = 0;
    }
    
    /**
     * Resetea la UI al estado inicial
     */
    private void resetUI() {
        tvDistance.setVisibility(View.GONE);
        tvSteps.setVisibility(View.GONE);
        tvTime.setVisibility(View.GONE);
        tvDistance.setText("0.00 km");
        tvSteps.setText(getString(R.string.steps) + ": 0");
        tvTime.setText(getString(R.string.time) + ": 00:00:00");
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTimeCounter();
    }
}
