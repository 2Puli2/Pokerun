package com.example.pokerun.data.repository;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.lifecycle.LiveData;

import com.example.pokerun.data.database.PokeRunDatabase;
import com.example.pokerun.data.database.dao.PokedexDao;
import com.example.pokerun.data.database.entity.PokedexEntryEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PokedexRepository {
    private PokedexDao pokedexDao;
    private Context context;
    
    public PokedexRepository(Context context) {
        PokeRunDatabase database = PokeRunDatabase.getDatabase(context);
        this.pokedexDao = database.pokedexDao();
        this.context = context;
    }
    
    public LiveData<List<PokedexEntryEntity>> getAllEntries() {
        return pokedexDao.getAllEntries();
    }
    
    public LiveData<PokedexEntryEntity> getEntryByNumber(int number) {
        return pokedexDao.getEntryByNumber(number);
    }
    
    public LiveData<List<PokedexEntryEntity>> getUnlockedEntries() {
        return pokedexDao.getUnlockedEntries();
    }
    
    /**
     * Inicializa los datos de la Pokédex solo si la base de datos está vacía.
     * Esto preserva los Pokémon desbloqueados por el usuario.
     */
    public void initializePokedex() {
        new Thread(() -> {
            // Solo cargar si la base de datos está vacía
            int count = pokedexDao.getCount();
            if (count == 0) {
                loadPokedexFromJson();
            }
        }).start();
    }
    
    private void loadPokedexFromJson() {
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
                for (PokedexData data : pokedexData) {
                    PokedexEntryEntity entry = new PokedexEntryEntity(
                        data.number,
                        data.name,
                        data.nameEn,
                        data.type1,
                        data.type2 != null && !data.type2.isEmpty() ? data.type2 : null,
                        data.description,
                        data.descriptionEn
                    );
                    pokedexDao.insert(entry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void unlockEntry(int number) {
        new Thread(() -> {
            pokedexDao.unlockEntry(number);
        }).start();
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

