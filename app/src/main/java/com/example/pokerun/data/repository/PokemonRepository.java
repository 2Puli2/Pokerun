package com.example.pokerun.data.repository;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.lifecycle.LiveData;

import com.example.pokerun.data.database.PokeRunDatabase;
import com.example.pokerun.data.database.dao.PokemonDao;
import com.example.pokerun.data.database.entity.PokemonEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Repositorio que gestiona el acceso a datos de Pokémon.
 * 
 * PATRÓN MVVM - CAPA MODEL (Repository):
 * Este repositorio implementa el patrón Repository, actuando como única
 * fuente de verdad para los datos de Pokémon. Abstrae las fuentes de datos
 * subyacentes (Room Database, archivos JSON) de los ViewModels.
 * 
 * RESPONSABILIDADES:
 * 1. Carga inicial de datos desde pokedex.json a Room Database
 * 2. Gestión de Pokémon obtenidos por el usuario
 * 3. Selección aleatoria de Pokémon para apertura de huevos
 * 4. Sistema de evolución de Pokémon
 * 
 * DECISIONES DE DISEÑO:
 * - OnConflictStrategy.IGNORE para preservar datos del usuario
 * - Evolución mediante actualización de atributos (no de ID)
 * - Filtrado de Pokémon por etapa evolutiva (solo base para huevos)
 * 
 * @see PokemonDao DAO que ejecuta las consultas SQL
 * @see PokemonEntity Entidad que representa un Pokémon
 * @see PokemonViewModel ViewModel que consume este repositorio
 */
public class PokemonRepository {
    private PokemonDao pokemonDao;
    private Context context;
    
    public PokemonRepository(Context context) {
        PokeRunDatabase database = PokeRunDatabase.getDatabase(context);
        this.pokemonDao = database.pokemonDao();
        this.context = context;
    }
    
    public LiveData<List<PokemonEntity>> getAllObtainedPokemon() {
        return pokemonDao.getAllObtainedPokemon();
    }
    
    public LiveData<PokemonEntity> getPokemonById(int id) {
        return pokemonDao.getPokemonById(id);
    }
    
    public LiveData<Integer> getObtainedCount() {
        return pokemonDao.getObtainedCount();
    }
    
    /**
     * Inicializa los datos de Pokémon solo si la base de datos está vacía.
     * Esto preserva los Pokémon obtenidos por el usuario.
     */
    public void initializePokemon() {
        new Thread(() -> {
            // Solo cargar si la base de datos está vacía
            int count = pokemonDao.getCount();
            if (count == 0) {
                loadPokemonFromJson();
            }
        }).start();
    }
    
    private void loadPokemonFromJson() {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("pokedex.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            
            Gson gson = new Gson();
            Type listType = new TypeToken<List<PokedexData>>(){}.getType();
            List<PokedexData> pokedexData = gson.fromJson(json, listType);
            
            if (pokedexData != null && !pokedexData.isEmpty()) {
                List<PokemonEntity> pokemonList = new ArrayList<>();
                for (PokedexData data : pokedexData) {
                    int evolutionStage = calculateEvolutionStage(data.number);
                    int evolvesFrom = getEvolvesFrom(data.number);
                    int evolvesTo = getEvolvesTo(data.number);
                    
                    PokemonEntity pokemon = new PokemonEntity(
                        data.number,
                        data.number,
                        data.name,
                        data.type1,
                        data.type2 != null && !data.type2.isEmpty() ? data.type2 : null,
                        evolutionStage,
                        evolvesFrom,
                        evolvesTo
                    );
                    pokemonList.add(pokemon);
                }
                
                if (!pokemonList.isEmpty()) {
                    pokemonDao.insertAll(pokemonList);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private int calculateEvolutionStage(int number) {
        // Lógica simple: si tiene evolución previa, es etapa 1 o 2
        int evolvesFrom = getEvolvesFrom(number);
        if (evolvesFrom == 0) return 0; // Base
        int evolvesFromStage = calculateEvolutionStage(evolvesFrom);
        return evolvesFromStage + 1;
    }
    
    private int getEvolvesFrom(int number) {
        // Implementación simplificada - debería cargarse desde evolutions.json
        // Por ahora, valores hardcodeados para los más comunes
        if (number == 2) return 1;
        if (number == 3) return 2;
        if (number == 5) return 4;
        if (number == 6) return 5;
        if (number == 8) return 7;
        if (number == 9) return 8;
        if (number == 11) return 10;
        if (number == 12) return 11;
        if (number == 14) return 13;
        if (number == 15) return 14;
        if (number == 17) return 16;
        if (number == 18) return 17;
        if (number == 20) return 19;
        if (number == 22) return 21;
        if (number == 24) return 23;
        if (number == 26) return 25;
        if (number == 28) return 27;
        if (number == 30) return 29;
        if (number == 31) return 30;
        if (number == 33) return 32;
        if (number == 34) return 33;
        if (number == 36) return 35;
        if (number == 38) return 37;
        if (number == 40) return 39;
        if (number == 42) return 41;
        if (number == 44) return 43;
        if (number == 45) return 44;
        if (number == 47) return 46;
        if (number == 49) return 48;
        if (number == 51) return 50;
        if (number == 53) return 52;
        if (number == 55) return 54;
        if (number == 57) return 56;
        if (number == 59) return 58;
        if (number == 61) return 60;
        if (number == 62) return 61;
        if (number == 64) return 63;
        if (number == 65) return 64;
        if (number == 67) return 66;
        if (number == 68) return 67;
        if (number == 70) return 69;
        if (number == 71) return 70;
        if (number == 73) return 72;
        if (number == 75) return 74;
        if (number == 76) return 75;
        if (number == 78) return 77;
        if (number == 80) return 79;
        if (number == 82) return 81;
        if (number == 85) return 84;
        if (number == 87) return 86;
        if (number == 89) return 88;
        if (number == 91) return 90;
        if (number == 93) return 92;
        if (number == 94) return 93;
        if (number == 97) return 96;
        if (number == 99) return 98;
        if (number == 101) return 100;
        if (number == 103) return 102;
        if (number == 105) return 104;
        if (number == 110) return 109;
        if (number == 112) return 111;
        if (number == 117) return 116;
        if (number == 119) return 118;
        if (number == 121) return 120;
        if (number == 130) return 129;
        if (number == 134) return 133;
        if (number == 135) return 133;
        if (number == 136) return 133;
        if (number == 139) return 138;
        if (number == 141) return 140;
        if (number == 148) return 147;
        if (number == 149) return 148;
        return 0;
    }
    
    private int getEvolvesTo(int number) {
        // Implementación simplificada
        if (number == 1) return 2;
        if (number == 2) return 3;
        if (number == 4) return 5;
        if (number == 5) return 6;
        if (number == 7) return 8;
        if (number == 8) return 9;
        if (number == 10) return 11;
        if (number == 11) return 12;
        if (number == 13) return 14;
        if (number == 14) return 15;
        if (number == 16) return 17;
        if (number == 17) return 18;
        if (number == 19) return 20;
        if (number == 21) return 22;
        if (number == 23) return 24;
        if (number == 25) return 26;
        if (number == 27) return 28;
        if (number == 29) return 30;
        if (number == 30) return 31;
        if (number == 32) return 33;
        if (number == 33) return 34;
        if (number == 35) return 36;
        if (number == 37) return 38;
        if (number == 39) return 40;
        if (number == 41) return 42;
        if (number == 43) return 44;
        if (number == 44) return 45;
        if (number == 46) return 47;
        if (number == 48) return 49;
        if (number == 50) return 51;
        if (number == 52) return 53;
        if (number == 54) return 55;
        if (number == 56) return 57;
        if (number == 58) return 59;
        if (number == 60) return 61;
        if (number == 61) return 62;
        if (number == 63) return 64;
        if (number == 64) return 65;
        if (number == 66) return 67;
        if (number == 67) return 68;
        if (number == 69) return 70;
        if (number == 70) return 71;
        if (number == 72) return 73;
        if (number == 74) return 75;
        if (number == 75) return 76;
        if (number == 77) return 78;
        if (number == 79) return 80;
        if (number == 81) return 82;
        if (number == 84) return 85;
        if (number == 86) return 87;
        if (number == 88) return 89;
        if (number == 90) return 91;
        if (number == 92) return 93;
        if (number == 93) return 94;
        if (number == 96) return 97;
        if (number == 98) return 99;
        if (number == 100) return 101;
        if (number == 102) return 103;
        if (number == 104) return 105;
        if (number == 109) return 110;
        if (number == 111) return 112;
        if (number == 116) return 117;
        if (number == 118) return 119;
        if (number == 120) return 121;
        if (number == 129) return 130;
        if (number == 133) return 134; // Eevee puede evolucionar a varios
        if (number == 138) return 139;
        if (number == 140) return 141;
        if (number == 147) return 148;
        if (number == 148) return 149;
        return 0;
    }
    
    public PokemonEntity getRandomUnobtainedPokemon() {
        try {
            List<PokemonEntity> unobtained = pokemonDao.getUnobtainedPokemon();
            if (unobtained == null || unobtained.isEmpty()) {
                return null;
            }
            
            // Filtrar solo Pokémon de primera etapa evolutiva (evolutionStage == 0)
            List<PokemonEntity> baseStagePokemon = new ArrayList<>();
            for (PokemonEntity pokemon : unobtained) {
                if (pokemon.evolutionStage == 0) {
                    baseStagePokemon.add(pokemon);
                }
            }
            
            if (baseStagePokemon.isEmpty()) {
                return null;
            }
            
            Random random = new Random();
            return baseStagePokemon.get(random.nextInt(baseStagePokemon.size()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void markPokemonAsObtained(int pokemonId) {
        new Thread(() -> {
            pokemonDao.markAsObtained(pokemonId, System.currentTimeMillis());
        }).start();
    }
    
    /**
     * Evoluciona un Pokémon obtenido a su siguiente forma evolutiva.
     * En lugar de cambiar el ID (que causaría conflictos), actualizamos los datos del Pokémon.
     * @param pokemonId ID del Pokémon a evolucionar
     * @return true si la evolución fue exitosa, false en caso contrario
     */
    public boolean evolvePokemon(int pokemonId) {
        try {
            // Obtener el Pokémon actual
            PokemonEntity currentPokemon = pokemonDao.getPokemonByIdSync(pokemonId);
            if (currentPokemon == null || currentPokemon.evolvesTo == 0) {
                return false;
            }
            
            // Obtener los datos del Pokémon evolucionado
            PokemonEntity evolvedPokemon = pokemonDao.getPokemonByPokedexNumber(currentPokemon.evolvesTo);
            if (evolvedPokemon == null) {
                return false;
            }
            
            // Actualizar el Pokémon actual con los datos de su evolución
            pokemonDao.updatePokemonEvolution(
                pokemonId,
                evolvedPokemon.pokedexNumber,
                evolvedPokemon.name,
                evolvedPokemon.type1,
                evolvedPokemon.type2,
                evolvedPokemon.evolutionStage,
                evolvedPokemon.evolvesFrom,
                evolvedPokemon.evolvesTo
            );
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtiene el nombre del Pokémon evolucionado
     */
    public String getEvolvedPokemonName(int pokemonId) {
        try {
            PokemonEntity currentPokemon = pokemonDao.getPokemonByIdSync(pokemonId);
            if (currentPokemon == null || currentPokemon.evolvesTo == 0) {
                return null;
            }
            PokemonEntity evolvedPokemon = pokemonDao.getPokemonByPokedexNumber(currentPokemon.evolvesTo);
            return evolvedPokemon != null ? evolvedPokemon.name : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Verifica si un Pokémon puede evolucionar
     */
    public boolean canEvolve(int pokemonId) {
        try {
            PokemonEntity pokemon = pokemonDao.getPokemonByIdSync(pokemonId);
            return pokemon != null && pokemon.evolvesTo > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static class PokedexData {
        int number;
        String name;
        String nameEn;
        String type1;
        String type2;
        String description;
        String descriptionEn;
    }
}

