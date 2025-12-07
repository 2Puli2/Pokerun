package com.example.pokerun;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import com.example.pokerun.data.database.PokeRunDatabase;
import com.example.pokerun.data.database.dao.BagDao;
import com.example.pokerun.data.database.dao.UserSettingsDao;
import com.example.pokerun.data.database.entity.BagEntity;
import com.example.pokerun.data.database.entity.UserSettingsEntity;
import com.example.pokerun.data.repository.PokedexRepository;
import com.example.pokerun.data.repository.PokemonRepository;

import java.util.Locale;

/**
 * Clase Application personalizada para PokeRun.
 * 
 * Esta clase representa el punto de entrada de la aplicación y se encarga de
 * la inicialización de los componentes fundamentales siguiendo el patrón MVVM.
 * 
 * PATRÓN MVVM - CAPA DE DATOS:
 * La Application actúa como coordinador inicial, garantizando que la capa de
 * datos (Room Database, Repositorios) esté disponible antes de que cualquier
 * ViewModel intente acceder a ella.
 * 
 * Responsabilidades:
 * - Configuración del idioma español por defecto (internacionalización)
 * - Inicialización del singleton de Room Database
 * - Inicialización de la mochila del usuario (BagEntity)
 * - Carga inicial de datos de Pokémon y Pokédex desde archivos JSON
 * 
 * @see PokeRunDatabase Singleton de la base de datos Room
 * @see PokemonRepository Repositorio que gestiona los datos de Pokémon
 * @see PokedexRepository Repositorio que gestiona las entradas de la Pokédex
 */
public class PokeRunApplication extends Application {
    private static final String TAG = "PokeRunApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            // Paso 1: Inicializar base de datos
            PokeRunDatabase database = PokeRunDatabase.getDatabase(this);
            Log.d(TAG, "Base de datos inicializada");
            
            // Paso 2: Inicializar configuración de usuario y aplicar idioma
            initializeUserSettings(database);
            
            // Paso 3: Inicializar mochila de forma síncrona
            initializeBag(database);
            
            // Paso 4: Cargar datos de Pokémon y Pokédex en hilo de fondo
            loadInitialData();
            
        } catch (Exception e) {
            Log.e(TAG, "Error en la inicialización de la aplicación", e);
        }
    }
    
    /**
     * Inicializa la configuración del usuario y aplica el idioma.
     * Por defecto: español (es) y kilómetros (km).
     */
    private void initializeUserSettings(PokeRunDatabase database) {
        try {
            UserSettingsDao settingsDao = database.userSettingsDao();
            UserSettingsEntity settings = settingsDao.getSettingsSync();
            
            if (settings == null) {
                // Crear configuración por defecto: español y kilómetros
                settings = new UserSettingsEntity("es", "km");
                settingsDao.insert(settings);
                Log.d(TAG, "Configuración inicial creada: español, km");
            }
            
            // Aplicar el idioma guardado
            applyLanguage(settings.language);
            
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando configuración", e);
            // En caso de error, aplicar español por defecto
            applyLanguage("es");
        }
    }
    
    /**
     * Aplica el idioma a toda la aplicación
     */
    private void applyLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        
        Configuration config = new Configuration(getResources().getConfiguration());
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        
        Log.d(TAG, "Idioma aplicado: " + languageCode);
    }
    
    /**
     * Inicializa la mochila del usuario si no existe.
     * Se ejecuta de forma síncrona para garantizar que esté disponible.
     */
    private void initializeBag(PokeRunDatabase database) {
        try {
            BagDao bagDao = database.bagDao();
            BagEntity bag = bagDao.getBagSync();
            
            if (bag == null) {
                BagEntity newBag = new BagEntity(0, 0);
                bagDao.insert(newBag);
                Log.d(TAG, "Mochila creada con éxito");
            } else {
                Log.d(TAG, "Mochila existente: " + bag.eggs + " huevos, " + bag.rareCandies + " caramelos");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando mochila", e);
        }
    }
    
    /**
     * Carga los datos iniciales de Pokémon y Pokédex desde los archivos JSON.
     * Se ejecuta en un hilo de fondo para no bloquear el arranque.
     */
    private void loadInitialData() {
        new Thread(() -> {
            try {
                // Cargar datos de Pokémon
                PokemonRepository pokemonRepository = new PokemonRepository(this);
                pokemonRepository.initializePokemon();
                Log.d(TAG, "Datos de Pokémon cargados");
                
                // Cargar datos de Pokédex
                PokedexRepository pokedexRepository = new PokedexRepository(this);
                pokedexRepository.initializePokedex();
                Log.d(TAG, "Datos de Pokédex cargados");
                
            } catch (Exception e) {
                Log.e(TAG, "Error cargando datos iniciales", e);
            }
        }).start();
    }
}
