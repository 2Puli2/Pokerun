package com.example.pokerun.data.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidad Room que representa una entrada de la Pokédex.
 * 
 * PATRÓN MVVM - CAPA MODEL (Entity):
 * Esta clase define la estructura de la tabla 'pokedex_entries' en Room Database.
 * Contiene información completa de los 151 Pokémon con soporte multiidioma.
 * 
 * INTERNACIONALIZACIÓN:
 * La entidad almacena nombres y descripciones en dos idiomas:
 * - name/description: Español (idioma por defecto)
 * - nameEn/descriptionEn: Inglés
 * 
 * El PokedexAdapter y PokedexViewModel consultan las preferencias del usuario
 * para mostrar el contenido en el idioma seleccionado.
 * 
 * ESTADO DE DESBLOQUEO:
 * El campo isUnlocked indica si el usuario ha obtenido ese Pokémon.
 * Se utiliza para diferenciar visualmente las entradas en la UI:
 * - Desbloqueado: colores vivos, borde rosa
 * - Bloqueado: escala de grises, opacidad reducida
 * 
 * @see PokedexDao DAO con operaciones de consulta
 * @see PokedexRepository Repositorio que gestiona la Pokédex
 * @see PokedexAdapter Adapter que renderiza las entradas
 */
@Entity(tableName = "pokedex_entries")
public class PokedexEntryEntity {
    /** Número en la Pokédex oficial (1-151) - Primary Key */
    @PrimaryKey
    public int pokedexNumber;
    
    /** Nombre del Pokémon en español */
    public String name;
    /** Nombre del Pokémon en inglés */
    public String nameEn;
    /** Tipo primario del Pokémon */
    public String type1;
    /** Tipo secundario del Pokémon (puede ser null) */
    public String type2;
    /** Descripción del Pokémon en español */
    public String description;
    /** Descripción del Pokémon en inglés */
    public String descriptionEn;
    /** Indica si el usuario ha desbloqueado esta entrada */
    public boolean isUnlocked;
    
    public PokedexEntryEntity() {}
    
    public PokedexEntryEntity(int pokedexNumber, String name, String nameEn, 
                             String type1, String type2, String description, String descriptionEn) {
        this.pokedexNumber = pokedexNumber;
        this.name = name;
        this.nameEn = nameEn;
        this.type1 = type1;
        this.type2 = type2;
        this.description = description;
        this.descriptionEn = descriptionEn;
        this.isUnlocked = false;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PokedexEntryEntity that = (PokedexEntryEntity) obj;
        return pokedexNumber == that.pokedexNumber;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(pokedexNumber);
    }
}

