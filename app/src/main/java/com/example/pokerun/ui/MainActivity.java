package com.example.pokerun.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.pokerun.R;
import com.example.pokerun.data.database.PokeRunDatabase;
import com.example.pokerun.data.database.entity.UserSettingsEntity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

/**
 * Activity principal de la aplicación PokeRun.
 * 
 * PATRÓN MVVM - CAPA VIEW:
 * Esta Activity implementa el patrón "Single Activity" recomendado por Google,
 * actuando como contenedor para múltiples Fragments. La navegación entre
 * pantallas se gestiona mediante Navigation Component, manteniendo la Activity
 * libre de lógica de negocio.
 * 
 * La Activity delega toda la lógica de presentación a los ViewModels asociados
 * a cada Fragment, limitándose a:
 * - Configurar la navegación con BottomNavigationView
 * - Aplicar preferencias de idioma del usuario
 * - Cargar elementos decorativos de la UI
 * 
 * @see WorkoutFragment Fragment para registro de entrenamientos
 * @see BagFragment Fragment para gestión de la mochila
 * @see PokemonFragment Fragment para lista de Pokémon obtenidos
 * @see PokedexFragment Fragment para visualizar la Pokédex
 * @see SettingsFragment Fragment para configuración de la aplicación
 */
public class MainActivity extends AppCompatActivity {
    
    // URL de la imagen de Mewtwo (artwork oficial)
    private static final String MEWTWO_IMAGE_URL = 
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/150.png";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        loadBackgroundImage();
        setupNavigation();
    }
    
    /**
     * Carga la imagen de Mewtwo como fondo decorativo
     */
    private void loadBackgroundImage() {
        ImageView ivBackground = findViewById(R.id.iv_background);
        if (ivBackground != null) {
            Glide.with(this)
                .load(MEWTWO_IMAGE_URL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivBackground);
        }
    }
    
    /**
     * Aplica el idioma guardado en la configuración del usuario
     */
    private void applyLanguage() {
        try {
            PokeRunDatabase database = PokeRunDatabase.getDatabase(this);
            UserSettingsEntity settings = database.userSettingsDao().getSettingsSync();
            
            String language = (settings != null && settings.language != null) 
                ? settings.language 
                : "es";
            
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            
            Configuration config = new Configuration(getResources().getConfiguration());
            config.setLocale(locale);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
            
        } catch (Exception e) {
            Locale locale = new Locale("es");
            Locale.setDefault(locale);
            Configuration config = new Configuration(getResources().getConfiguration());
            config.setLocale(locale);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }
    }
    
    /**
     * Configura la navegación con BottomNavigationView
     */
    private void setupNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }
    }
}
