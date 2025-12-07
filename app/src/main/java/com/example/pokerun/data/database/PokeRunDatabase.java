package com.example.pokerun.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.pokerun.data.database.dao.BagDao;
import com.example.pokerun.data.database.dao.PokedexDao;
import com.example.pokerun.data.database.dao.PokemonDao;
import com.example.pokerun.data.database.dao.UserSettingsDao;
import com.example.pokerun.data.database.dao.WorkoutDao;
import com.example.pokerun.data.database.entity.BagEntity;
import com.example.pokerun.data.database.entity.PokedexEntryEntity;
import com.example.pokerun.data.database.entity.PokemonEntity;
import com.example.pokerun.data.database.entity.UserSettingsEntity;
import com.example.pokerun.data.database.entity.WorkoutEntity;

/**
 * Base de datos Room para la aplicación PokeRun.
 * 
 * PATRÓN MVVM - CAPA MODEL (Data Source):
 * Esta clase representa la fuente de datos local de la aplicación, implementando
 * el patrón Singleton para garantizar una única instancia de la base de datos
 * en toda la aplicación.
 * 
 * Room Database proporciona:
 * - Abstracción sobre SQLite con verificación en tiempo de compilación
 * - Soporte nativo para LiveData (observabilidad reactiva)
 * - Conversión automática de entidades Java a tablas SQL
 * 
 * La base de datos contiene 5 tablas principales:
 * - pokemon: Pokémon disponibles y estado de obtención
 * - pokedex_entries: Información completa de la Pokédex
 * - bag: Inventario del usuario (huevos, caramelos)
 * - workouts: Historial de entrenamientos
 * - user_settings: Preferencias del usuario
 * 
 * @see PokemonEntity Entidad que representa un Pokémon
 * @see PokedexEntryEntity Entidad para entradas de la Pokédex
 * @see BagEntity Entidad para el inventario del usuario
 * @see WorkoutEntity Entidad para los entrenamientos
 * @see UserSettingsEntity Entidad para las preferencias
 */
@Database(
    entities = {
        PokemonEntity.class,
        PokedexEntryEntity.class,
        BagEntity.class,
        WorkoutEntity.class,
        UserSettingsEntity.class
    },
    version = 1,
    exportSchema = false
)
@TypeConverters({})
public abstract class PokeRunDatabase extends RoomDatabase {
    private static volatile PokeRunDatabase INSTANCE;
    
    public abstract PokemonDao pokemonDao();
    public abstract PokedexDao pokedexDao();
    public abstract BagDao bagDao();
    public abstract WorkoutDao workoutDao();
    public abstract UserSettingsDao userSettingsDao();
    
    public static PokeRunDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PokeRunDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        PokeRunDatabase.class,
                        "pokerun_database"
                    )
                    .allowMainThreadQueries() // Permitir queries en el hilo principal temporalmente
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}

