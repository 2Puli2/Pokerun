package com.example.pokerun.ui.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pokerun.R;
import com.example.pokerun.data.database.entity.UserSettingsEntity;
import com.example.pokerun.ui.viewmodel.SettingsViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

/**
 * Fragment de configuración de la aplicación.
 * 
 * PATRÓN MVVM - CAPA VIEW:
 * Este Fragment gestiona las preferencias del usuario, demostrando
 * cómo MVVM facilita la persistencia y aplicación de configuraciones
 * a través de todas las capas de la aplicación.
 * 
 * PREFERENCIAS DISPONIBLES:
 * 1. Idioma: Español (es) / Inglés (en)
 *    - Afecta a toda la UI de la aplicación
 *    - Afecta a los nombres y descripciones de la Pokédex
 *    - Se aplica mediante cambio dinámico de Locale
 *    - Requiere recrear la Activity para aplicar cambios
 * 
 * 2. Unidad de distancia: Kilómetros (km) / Millas (mi)
 *    - Afecta a la visualización de distancias en entrenamientos
 *    - Factor de conversión: 1 km = 0.621371 millas
 *    - Se aplica inmediatamente sin reinicio
 * 
 * PERSISTENCIA:
 * Las preferencias se almacenan en UserSettingsEntity mediante Room,
 * garantizando que sobrevivan al cierre de la aplicación.
 * 
 * @see SettingsViewModel ViewModel que gestiona las preferencias
 * @see UserSettingsRepository Repositorio de preferencias
 * @see UserSettingsEntity Entidad que persiste las preferencias
 */
public class SettingsFragment extends Fragment {
    
    private SettingsViewModel viewModel;
    
    // Botones de idioma
    private MaterialButton btnLanguageEs;
    private MaterialButton btnLanguageEn;
    
    // Botones de unidad de distancia
    private MaterialButton btnUnitKm;
    private MaterialButton btnUnitMi;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeComponents(view);
        setupListeners();
        setupObservers();
    }
    
    /**
     * Inicializa los componentes del fragment
     */
    private void initializeComponents(@NonNull View view) {
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        
        btnLanguageEs = view.findViewById(R.id.btn_language_es);
        btnLanguageEn = view.findViewById(R.id.btn_language_en);
        btnUnitKm = view.findViewById(R.id.btn_unit_km);
        btnUnitMi = view.findViewById(R.id.btn_unit_mi);
    }
    
    /**
     * Configura los listeners de los botones
     */
    private void setupListeners() {
        btnLanguageEs.setOnClickListener(v -> changeLanguage("es"));
        btnLanguageEn.setOnClickListener(v -> changeLanguage("en"));
        btnUnitKm.setOnClickListener(v -> changeDistanceUnit("km"));
        btnUnitMi.setOnClickListener(v -> changeDistanceUnit("mi"));
    }
    
    /**
     * Configura los observadores de LiveData
     */
    private void setupObservers() {
        viewModel.getSettings().observe(getViewLifecycleOwner(), this::updateButtonStates);
    }
    
    /**
     * Cambia el idioma de la aplicación
     * @param language Código del idioma ("es" o "en")
     */
    private void changeLanguage(String language) {
        viewModel.updateLanguage(language);
        
        // Aplicar el cambio de idioma
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration(getResources().getConfiguration());
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        
        // Mostrar confirmación
        String langName = "es".equals(language) ? getString(R.string.spanish) : getString(R.string.english);
        Toast.makeText(getContext(), getString(R.string.language_changed, langName), Toast.LENGTH_SHORT).show();
        
        // Refrescar la actividad para aplicar cambios
        if (getActivity() != null) {
            getActivity().recreate();
        }
    }
    
    /**
     * Cambia la unidad de distancia
     * @param unit Código de unidad ("km" o "mi")
     */
    private void changeDistanceUnit(String unit) {
        viewModel.updateDistanceUnit(unit);
        
        String unitName = "km".equals(unit) ? getString(R.string.kilometers) : getString(R.string.miles);
        Toast.makeText(getContext(), getString(R.string.unit_changed, unitName), Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Actualiza el estado visual de los botones según la configuración actual
     */
    private void updateButtonStates(UserSettingsEntity settings) {
        if (settings == null) return;
        
        // Actualizar botones de idioma
        boolean isSpanish = "es".equals(settings.language);
        btnLanguageEs.setEnabled(!isSpanish);
        btnLanguageEn.setEnabled(isSpanish);
        
        // Actualizar botones de unidad
        boolean isKm = "km".equals(settings.distanceUnit);
        btnUnitKm.setEnabled(!isKm);
        btnUnitMi.setEnabled(isKm);
    }
}
