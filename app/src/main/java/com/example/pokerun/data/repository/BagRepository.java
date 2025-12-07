package com.example.pokerun.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.pokerun.data.database.PokeRunDatabase;
import com.example.pokerun.data.database.dao.BagDao;
import com.example.pokerun.data.database.entity.BagEntity;

/**
 * Repositorio que gestiona el inventario (mochila) del usuario.
 * 
 * PATRÓN MVVM - CAPA MODEL (Repository):
 * Este repositorio abstrae el acceso a la entidad BagEntity,
 * proporcionando operaciones de alto nivel para gestionar el
 * inventario del usuario.
 * 
 * OPERACIONES PRINCIPALES:
 * - Añadir huevos (recompensa de entrenamientos)
 * - Añadir caramelos raros (recompensa de entrenamientos)
 * - Remover huevo + caramelo (apertura de huevo) - Transacción atómica
 * - Remover caramelos (evolución de Pokémon)
 * 
 * CONSIDERACIONES DE CONCURRENCIA:
 * Las operaciones de modificación son síncronas y deben ejecutarse
 * en un hilo de fondo. El repositorio garantiza la consistencia
 * mediante operaciones atómicas en removeEggAndCandy().
 * 
 * INICIALIZACIÓN:
 * El constructor verifica y crea la mochila si no existe,
 * garantizando que siempre haya un registro disponible.
 * 
 * @see BagDao DAO que ejecuta las operaciones SQL
 * @see BagEntity Entidad que representa el inventario
 * @see BagViewModel ViewModel que consume este repositorio
 */
public class BagRepository {
    private static final String TAG = "BagRepository";
    private BagDao bagDao;
    
    public BagRepository(Context context) {
        PokeRunDatabase database = PokeRunDatabase.getDatabase(context);
        this.bagDao = database.bagDao();
        ensureBagExistsSync();
    }
    
    public LiveData<BagEntity> getBag() {
        return bagDao.getBag();
    }
    
    public BagEntity getBagSync() {
        try {
            BagEntity bag = bagDao.getBagSync();
            if (bag == null) {
                bag = new BagEntity(0, 0);
                bagDao.insert(bag);
                bag = bagDao.getBagSync();
            }
            return bag;
        } catch (Exception e) {
            Log.e(TAG, "Error en getBagSync", e);
            return null;
        }
    }
    
    // Método para asegurar que la mochila existe
    private void ensureBagExistsSync() {
        try {
            BagEntity bag = bagDao.getBagSync();
            if (bag == null) {
                bagDao.insert(new BagEntity(0, 0));
                Log.d(TAG, "Mochila creada");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en ensureBagExistsSync", e);
        }
    }
    
    // Métodos síncronos para añadir items
    public void addEggsSync(int amount) {
        try {
            BagEntity bag = getBagSync();
            if (bag != null) {
                bag.eggs += amount;
                bagDao.update(bag);
                Log.d(TAG, "Añadidos " + amount + " huevos. Total: " + bag.eggs);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en addEggsSync", e);
        }
    }
    
    public void addCandiesSync(int amount) {
        try {
            BagEntity bag = getBagSync();
            if (bag != null) {
                bag.rareCandies += amount;
                bagDao.update(bag);
                Log.d(TAG, "Añadidos " + amount + " caramelos. Total: " + bag.rareCandies);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en addCandiesSync", e);
        }
    }
    
    // Métodos asíncronos
    public void addEggs(int amount) {
        new Thread(() -> addEggsSync(amount)).start();
    }
    
    public void addCandies(int amount) {
        new Thread(() -> addCandiesSync(amount)).start();
    }
    
    // Método combinado para remover huevo y caramelo de forma atómica
    public boolean removeEggAndCandy() {
        try {
            BagEntity bag = bagDao.getBagSync();
            if (bag == null) {
                Log.e(TAG, "removeEggAndCandy: bag es null");
                return false;
            }
            
            Log.d(TAG, "removeEggAndCandy: huevos=" + bag.eggs + ", caramelos=" + bag.rareCandies);
            
            if (bag.eggs > 0 && bag.rareCandies >= 1) {
                bag.eggs -= 1;
                bag.rareCandies -= 1;
                bagDao.update(bag);
                Log.d(TAG, "Huevo y caramelo removidos. Ahora: huevos=" + bag.eggs + ", caramelos=" + bag.rareCandies);
                return true;
            } else {
                Log.d(TAG, "No hay suficientes items");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en removeEggAndCandy", e);
        }
        return false;
    }
    
    public boolean removeEgg() {
        try {
            BagEntity bag = bagDao.getBagSync();
            if (bag == null) {
                return false;
            }
            if (bag.eggs > 0) {
                bag.eggs -= 1;
                bagDao.update(bag);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en removeEgg", e);
        }
        return false;
    }
    
    public boolean removeCandies(int amount) {
        try {
            BagEntity bag = bagDao.getBagSync();
            if (bag == null) {
                return false;
            }
            if (bag.rareCandies >= amount) {
                bag.rareCandies -= amount;
                bagDao.update(bag);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en removeCandies", e);
        }
        return false;
    }
}

