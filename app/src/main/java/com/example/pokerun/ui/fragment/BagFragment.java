package com.example.pokerun.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pokerun.R;
import com.example.pokerun.data.database.entity.BagEntity;
import com.example.pokerun.data.database.entity.PokemonEntity;
import com.example.pokerun.data.repository.BagRepository;
import com.example.pokerun.data.repository.PokemonRepository;
import com.example.pokerun.ui.viewmodel.BagViewModel;

/**
 * Fragment que muestra el contenido de la mochila del usuario.
 * 
 * PATRÓN MVVM - CAPA VIEW:
 * Este Fragment implementa la Vista para la sección de inventario.
 * Observa los cambios en BagViewModel y actualiza la UI de forma reactiva.
 * 
 * FLUJO DE APERTURA DE HUEVO (MVVM en acción):
 * 1. Usuario pulsa "Abrir Huevo" → Fragment notifica al ViewModel
 * 2. ViewModel verifica requisitos (huevos >= 1, caramelos >= 1)
 * 3. ViewModel coordina con BagRepository y PokemonRepository
 * 4. BagRepository descuenta 1 huevo y 1 caramelo (transacción)
 * 5. PokemonRepository selecciona un Pokémon aleatorio no obtenido
 * 6. Se actualiza la Pokédex para desbloquear la nueva entrada
 * 7. ViewModel actualiza LiveData → Fragment muestra diálogo de éxito
 * 
 * NOTA ARQUITECTÓNICA:
 * La lógica de coordinación entre repositorios podría extraerse a un
 * UseCase/Interactor en una arquitectura Clean Architecture más pura.
 * 
 * @see BagViewModel ViewModel que gestiona la lógica del inventario
 * @see BagRepository Repositorio que accede a la entidad BagEntity
 * @see PokemonRepository Repositorio para obtener Pokémon aleatorios
 */
public class BagFragment extends Fragment {
    private static final String TAG = "BagFragment";
    
    // ViewModel y Repositorios
    private BagViewModel viewModel;
    private PokemonRepository pokemonRepository;
    private BagRepository bagRepository;
    
    // Vistas
    private TextView tvEggsCount;
    private TextView tvCandiesCount;
    private Button btnOpenEgg;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bag, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeComponents(view);
        setupObservers();
        setupListeners();
    }
    
    /**
     * Inicializa los componentes del fragment
     */
    private void initializeComponents(@NonNull View view) {
        viewModel = new ViewModelProvider(this).get(BagViewModel.class);
        pokemonRepository = new PokemonRepository(requireContext());
        bagRepository = new BagRepository(requireContext());
        
        tvEggsCount = view.findViewById(R.id.tv_eggs_count);
        tvCandiesCount = view.findViewById(R.id.tv_candies_count);
        btnOpenEgg = view.findViewById(R.id.btn_open_egg);
    }
    
    /**
     * Configura los observadores de LiveData
     */
    private void setupObservers() {
        viewModel.getBag().observe(getViewLifecycleOwner(), bag -> {
            if (bag != null) {
                tvEggsCount.setText(String.valueOf(bag.eggs));
                tvCandiesCount.setText(String.valueOf(bag.rareCandies));
            } else {
                tvEggsCount.setText("0");
                tvCandiesCount.setText("0");
            }
        });
    }
    
    /**
     * Configura los listeners de los botones
     */
    private void setupListeners() {
        btnOpenEgg.setOnClickListener(v -> openEgg());
    }
    
    @Override
    public void onResume() {
        super.onResume();
        refreshBagData();
    }
    
    /**
     * Refresca los datos de la mochila desde la base de datos
     */
    private void refreshBagData() {
        new Thread(() -> {
            try {
                BagEntity bag = bagRepository.getBagSync();
                if (bag != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvEggsCount.setText(String.valueOf(bag.eggs));
                        tvCandiesCount.setText(String.valueOf(bag.rareCandies));
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error refrescando datos de la mochila", e);
            }
        }).start();
    }
    
    /**
     * Abre un huevo y otorga un Pokémon aleatorio de primera etapa.
     * Requiere 1 huevo y 1 caramelo raro.
     */
    private void openEgg() {
        if (getContext() == null) return;
        
        // Deshabilitar botón mientras se procesa
        btnOpenEgg.setEnabled(false);
        
        new Thread(() -> {
            try {
                // Verificar estado actual de la mochila
                BagEntity bag = bagRepository.getBagSync();
                
                if (bag == null || bag.eggs <= 0) {
                    showToast(getString(R.string.no_eggs));
                    enableButton();
                    return;
                }
                
                if (bag.rareCandies < 1) {
                    showToast(getString(R.string.not_enough_candies));
                    enableButton();
                    return;
                }
                
                // Remover huevo y caramelo
                boolean success = bagRepository.removeEggAndCandy();
                
                if (!success) {
                    showToast(getString(R.string.error_opening_egg));
                    enableButton();
                    return;
                }
                
                // Obtener un Pokémon aleatorio de primera etapa
                PokemonEntity randomPokemon = pokemonRepository.getRandomUnobtainedPokemon();
                if (randomPokemon == null) {
                    showToast(getString(R.string.all_pokemon_obtained));
                    enableButton();
                    return;
                }
                
                // Marcar Pokémon como obtenido y desbloquear en Pokédex
                viewModel.markPokemonObtained(randomPokemon.id, randomPokemon.pokedexNumber);
                String pokemonName = randomPokemon.name;
                
                // Mostrar diálogo con el Pokémon obtenido
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (getContext() != null) {
                            showPokemonObtainedDialog(pokemonName);
                            refreshBagData();
                        }
                        btnOpenEgg.setEnabled(true);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error abriendo huevo", e);
                showToast("Error: " + e.getMessage());
                enableButton();
            }
        }).start();
    }
    
    /**
     * Muestra un Toast en el hilo de UI
     */
    private void showToast(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    /**
     * Habilita el botón de abrir huevo en el hilo de UI
     */
    private void enableButton() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> btnOpenEgg.setEnabled(true));
        }
    }
    
    /**
     * Muestra un diálogo de éxito con el Pokémon obtenido
     */
    private void showPokemonObtainedDialog(String pokemonName) {
        new AlertDialog.Builder(getContext())
            .setTitle(R.string.egg_opened_title)
            .setMessage(getString(R.string.egg_opened_message, pokemonName))
            .setPositiveButton(R.string.ok, null)
            .setIcon(android.R.drawable.star_big_on)
            .show();
    }
}
