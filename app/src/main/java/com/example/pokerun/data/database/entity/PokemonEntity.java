package com.example.pokerun.data.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidad Room que representa un Pokémon en la base de datos.
 * 
 * PATRÓN MVVM - CAPA MODEL (Entity):
 * Esta clase define la estructura de la tabla 'pokemon' en Room Database.
 * Cada instancia representa un Pokémon de la primera generación (151 total).
 * 
 * CAMPOS PRINCIPALES:
 * - id: Identificador único (Primary Key)
 * - pokedexNumber: Número en la Pokédex oficial (1-151)
 * - name: Nombre del Pokémon en el idioma actual
 * - type1, type2: Tipos del Pokémon (ej: "Fire", "Flying")
 * - evolutionStage: Etapa evolutiva (0=base, 1=primera, 2=segunda)
 * - evolvesFrom/evolvesTo: Cadena evolutiva
 * - isObtained: Si el usuario ha obtenido este Pokémon
 * - obtainedDate: Timestamp de obtención
 * 
 * NOTA SOBRE EVOLUCIÓN:
 * Al evolucionar, se actualizan los atributos (pokedexNumber, name, tipos, etc.)
 * pero se mantiene el mismo ID para preservar la integridad referencial.
 * 
 * @see PokemonDao DAO con operaciones CRUD
 * @see PokemonRepository Repositorio que abstrae el acceso a datos
 */
@Entity(tableName = "pokemon")
public class PokemonEntity {
    @PrimaryKey
    public int id;
    
    /** Número en la Pokédex oficial (1-151) */
    public int pokedexNumber;
    /** Nombre del Pokémon en español */
    public String name;
    /** Tipo primario del Pokémon */
    public String type1;
    /** Tipo secundario del Pokémon (puede ser null) */
    public String type2;
    /** Etapa evolutiva: 0 = base, 1 = primera evolución, 2 = segunda evolución */
    public int evolutionStage;
    /** Número de Pokédex del que evoluciona (0 si es forma base) */
    public int evolvesFrom;
    /** Número de Pokédex al que evoluciona (0 si no evoluciona más) */
    public int evolvesTo;
    /** Indica si el usuario ha obtenido este Pokémon */
    public boolean isObtained;
    /** Timestamp de cuando el usuario obtuvo este Pokémon */
    public long obtainedDate;
    
    public PokemonEntity() {}
    
    public PokemonEntity(int id, int pokedexNumber, String name, String type1, String type2, 
                        int evolutionStage, int evolvesFrom, int evolvesTo) {
        this.id = id;
        this.pokedexNumber = pokedexNumber;
        this.name = name;
        this.type1 = type1;
        this.type2 = type2;
        this.evolutionStage = evolutionStage;
        this.evolvesFrom = evolvesFrom;
        this.evolvesTo = evolvesTo;
        this.isObtained = false;
        this.obtainedDate = 0;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PokemonEntity that = (PokemonEntity) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}

