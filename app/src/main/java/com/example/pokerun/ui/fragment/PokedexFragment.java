package com.example.pokerun.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokerun.R;
import com.example.pokerun.data.database.entity.PokedexEntryEntity;
import com.example.pokerun.data.repository.UserSettingsRepository;
import com.example.pokerun.ui.adapter.PokedexAdapter;
import com.example.pokerun.ui.viewmodel.PokedexViewModel;

import java.util.List;

/**
 * Fragment que muestra la Pokédex completa con los 151 Pokémon de primera generación.
 * 
 * PATRÓN MVVM - CAPA VIEW:
 * Este Fragment demuestra cómo una Vista puede observar datos complejos
 * (lista de 151 entradas) y actualizarse de forma eficiente gracias a:
 * - LiveData para observabilidad reactiva
 * - DiffUtil (implementado en PokedexAdapter) para actualizaciones óptimas
 * - RecyclerView con view recycling para rendimiento
 * 
 * CARACTERÍSTICAS VISUALES:
 * - Pokémon desbloqueados: colores vivos, borde rosa (masterball_pink)
 * - Pokémon bloqueados: escala de grises, opacidad reducida
 * - Número de Pokédex siempre visible con fondo contrastante
 * - Tipos con colores oficiales (Agua=azul, Fuego=naranja, etc.)
 * - Contador de progreso: "X/151 Pokémon obtenidos"
 * - Barra de progreso visual
 * 
 * INTERNACIONALIZACIÓN:
 * Los nombres y descripciones se muestran en el idioma configurado
 * por el usuario (español/inglés), demostrando cómo MVVM facilita
 * la internacionalización al centralizar la lógica en el ViewModel.
 * 
 * @see PokedexViewModel ViewModel que expone las entradas de la Pokédex
 * @see PokedexAdapter Adapter con DiffUtil para actualizaciones eficientes
 * @see PokedexEntryEntity Entidad con soporte multiidioma
 */
public class PokedexFragment extends Fragment {
    
    private static final int TOTAL_POKEMON = 151;
    
    private PokedexViewModel viewModel;
    private RecyclerView rvPokedex;
    private PokedexAdapter adapter;
    private UserSettingsRepository settingsRepository;
    
    // Elementos del header
    private TextView tvObtainedCount;
    private TextView tvTotalCount;
    private ProgressBar progressPokedex;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pokedex, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(PokedexViewModel.class);
        settingsRepository = new UserSettingsRepository(requireContext());
        
        // Inicializar vistas
        initViews(view);
        
        // Configurar RecyclerView
        setupRecyclerView();
        
        // Observar datos
        observeData();
    }
    
    /**
     * Inicializa todas las vistas del fragmento
     */
    private void initViews(View view) {
        rvPokedex = view.findViewById(R.id.rv_pokedex);
        tvObtainedCount = view.findViewById(R.id.tv_obtained_count);
        tvTotalCount = view.findViewById(R.id.tv_total_count);
        progressPokedex = view.findViewById(R.id.progress_pokedex);
        
        // Configurar valores iniciales
        tvTotalCount.setText(String.valueOf(TOTAL_POKEMON));
        progressPokedex.setMax(TOTAL_POKEMON);
    }
    
    /**
     * Configura el RecyclerView con el adapter
     */
    private void setupRecyclerView() {
        adapter = new PokedexAdapter(settingsRepository);
        rvPokedex.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPokedex.setAdapter(adapter);
        
        // Optimizaciones de rendimiento
        rvPokedex.setHasFixedSize(true);
        rvPokedex.setItemViewCacheSize(20);
    }
    
    /**
     * Observa los cambios en los datos de la Pokédex
     */
    private void observeData() {
        viewModel.getAllEntries().observe(getViewLifecycleOwner(), entries -> {
            if (entries != null) {
                adapter.submitList(entries);
                updateProgress(entries);
            }
        });
    }
    
    /**
     * Actualiza el contador y la barra de progreso
     */
    private void updateProgress(List<PokedexEntryEntity> entries) {
        int obtainedCount = 0;
        
        for (PokedexEntryEntity entry : entries) {
            if (entry.isUnlocked) {
                obtainedCount++;
            }
        }
        
        // Actualizar UI
        tvObtainedCount.setText(String.valueOf(obtainedCount));
        progressPokedex.setProgress(obtainedCount);
    }
}
