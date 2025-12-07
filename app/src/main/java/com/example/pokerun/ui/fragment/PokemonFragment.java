package com.example.pokerun.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokerun.R;
import com.example.pokerun.data.database.entity.PokemonEntity;
import com.example.pokerun.data.repository.BagRepository;
import com.example.pokerun.data.repository.PokedexRepository;
import com.example.pokerun.data.repository.PokemonRepository;
import com.example.pokerun.ui.adapter.PokemonAdapter;
import com.example.pokerun.ui.viewmodel.PokemonViewModel;

/**
 * Fragment que muestra la lista de Pokémon obtenidos por el usuario.
 * 
 * PATRÓN MVVM - CAPA VIEW:
 * Implementa la visualización de la colección de Pokémon del usuario,
 * utilizando un RecyclerView con PokemonAdapter y observando datos
 * desde PokemonViewModel.
 * 
 * SISTEMA DE EVOLUCIÓN:
 * El flujo de evolución demuestra la coordinación entre capas MVVM:
 * 1. Usuario selecciona Pokémon → Fragment muestra diálogo de confirmación
 * 2. Usuario confirma → Fragment llama a ViewModel.evolvePokemon()
 * 3. ViewModel verifica caramelos disponibles con BagRepository
 * 4. Si hay caramelos, PokemonRepository ejecuta la evolución
 * 5. PokedexRepository desbloquea la nueva entrada
 * 6. ViewModel actualiza LiveData → UI se refresca automáticamente
 * 
 * DECISIÓN TÉCNICA:
 * La evolución actualiza los atributos del registro existente en lugar
 * de cambiar su ID, evitando violaciones de UNIQUE constraint y
 * preservando la integridad referencial de la base de datos.
 * 
 * @see PokemonViewModel ViewModel que expone los Pokémon obtenidos
 * @see PokemonAdapter Adapter que renderiza cada Pokémon en el RecyclerView
 * @see PokemonRepository Repositorio que gestiona las evoluciones
 */
public class PokemonFragment extends Fragment {
    private static final String TAG = "PokemonFragment";
    
    private PokemonViewModel viewModel;
    private PokemonRepository pokemonRepository;
    private BagRepository bagRepository;
    private PokedexRepository pokedexRepository;
    
    private RecyclerView rvPokemon;
    private TextView tvEmpty;
    private PokemonAdapter adapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pokemon, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeComponents(view);
        setupRecyclerView();
        setupObservers();
    }
    
    private void initializeComponents(@NonNull View view) {
        viewModel = new ViewModelProvider(this).get(PokemonViewModel.class);
        pokemonRepository = new PokemonRepository(requireContext());
        bagRepository = new BagRepository(requireContext());
        pokedexRepository = new PokedexRepository(requireContext());
        
        rvPokemon = view.findViewById(R.id.rv_pokemon);
        tvEmpty = view.findViewById(R.id.tv_empty);
    }
    
    private void setupRecyclerView() {
        adapter = new PokemonAdapter(pokemon -> {
            if (pokemon.evolvesTo > 0) {
                showEvolveConfirmDialog(pokemon);
            }
        });
        
        rvPokemon.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPokemon.setAdapter(adapter);
    }
    
    private void setupObservers() {
        viewModel.getObtainedPokemon().observe(getViewLifecycleOwner(), pokemonList -> {
            if (pokemonList != null && !pokemonList.isEmpty()) {
                adapter.submitList(pokemonList);
                tvEmpty.setVisibility(View.GONE);
                rvPokemon.setVisibility(View.VISIBLE);
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
                rvPokemon.setVisibility(View.GONE);
            }
        });
    }
    
    /**
     * Muestra el diálogo de confirmación para evolucionar un Pokémon
     */
    private void showEvolveConfirmDialog(PokemonEntity pokemon) {
        new Thread(() -> {
            String evolvedName = pokemonRepository.getEvolvedPokemonName(pokemon.id);
            int evolvedPokedexNumber = pokemon.evolvesTo;
            
            if (evolvedName == null) {
                showToast(getString(R.string.not_enough_candies_evolve));
                return;
            }
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (getContext() == null) return;
                    
                    new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.evolve))
                        .setMessage(getString(R.string.evolution_confirm, pokemon.name, evolvedName))
                        .setPositiveButton(R.string.evolve, (dialog, which) -> 
                            evolvePokemon(pokemon, evolvedName, evolvedPokedexNumber))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                });
            }
        }).start();
    }
    
    /**
     * Ejecuta la evolución de un Pokémon
     */
    private void evolvePokemon(PokemonEntity pokemon, String evolvedName, int evolvedPokedexNumber) {
        new Thread(() -> {
            try {
                var bag = bagRepository.getBagSync();
                if (bag == null || bag.rareCandies < 1) {
                    showToast(getString(R.string.not_enough_candies_evolve));
                    return;
                }
                
                boolean candyRemoved = bagRepository.removeCandies(1);
                if (!candyRemoved) {
                    showToast(getString(R.string.not_enough_candies_evolve));
                    return;
                }
                
                boolean success = pokemonRepository.evolvePokemon(pokemon.id);
                
                if (success) {
                    pokedexRepository.unlockEntry(evolvedPokedexNumber);
                    
                    // Mostrar diálogo de éxito centrado
                    showEvolutionSuccessDialog(pokemon.name, evolvedName);
                } else {
                    bagRepository.addCandiesSync(1);
                    showToast(getString(R.string.error_opening_egg));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error evolucionando Pokémon", e);
                showToast("Error: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Muestra un diálogo centrado con el resultado de la evolución
     */
    private void showEvolutionSuccessDialog(String originalName, String evolvedName) {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            if (getContext() == null) return;
            
            String message = getString(R.string.evolution_success, originalName, evolvedName);
            
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.evolution_success_title))
                .setMessage(message)
                .setPositiveButton(R.string.ok, (d, which) -> d.dismiss())
                .setCancelable(false)
                .create();
            
            dialog.show();
            
            // Centrar el diálogo y ajustar su apariencia
            Window window = dialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.CENTER);
                WindowManager.LayoutParams params = window.getAttributes();
                params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(params);
            }
        });
    }
    
    private void showToast(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
