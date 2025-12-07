package com.example.pokerun.ui.adapter;

import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.pokerun.R;
import com.example.pokerun.data.database.entity.PokedexEntryEntity;
import com.example.pokerun.data.repository.UserSettingsRepository;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter para mostrar las entradas de la Pokédex en un RecyclerView.
 * 
 * PATRÓN MVVM - COMPONENTE DE LA VISTA:
 * Este Adapter extiende ListAdapter, que utiliza internamente DiffUtil
 * para calcular las diferencias entre listas y actualizar solo los
 * elementos que han cambiado, optimizando el rendimiento.
 * 
 * ALGORITMO DIFFUTIL:
 * DiffUtil implementa el algoritmo de Myers para encontrar la secuencia
 * mínima de ediciones entre dos listas. Complejidad: O(N + D²) donde
 * D es el número de ediciones. Esto permite actualizaciones eficientes
 * cuando se desbloquea un Pokémon sin recargar toda la lista.
 * 
 * DIFERENCIACIÓN VISUAL:
 * - Pokémon DESBLOQUEADO:
 *   · Colores vivos en tipos e imagen
 *   · Borde rosa (masterball_pink) con stroke de 4dp
 *   · Opacidad completa (alpha = 1.0)
 *   · Icono de desbloqueo visible
 * 
 * - Pokémon BLOQUEADO:
 *   · Escala de grises (ColorMatrixColorFilter)
 *   · Borde gris con stroke de 1dp
 *   · Opacidad reducida (alpha = 0.5 en imagen)
 *   · Sin icono de desbloqueo
 * 
 * @see PokedexFragment Fragment que utiliza este adapter
 * @see PokedexEntryEntity Entidad que representa cada entrada
 * @see PokedexDiffCallback Callback que compara entradas
 */
public class PokedexAdapter extends ListAdapter<PokedexEntryEntity, PokedexAdapter.PokedexViewHolder> {
    
    private static final String POKEMON_SPRITE_URL = 
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/";
    
    private final UserSettingsRepository settingsRepository;
    
    // Filtro de escala de grises
    private static final ColorMatrixColorFilter GRAYSCALE_FILTER;
    static {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        GRAYSCALE_FILTER = new ColorMatrixColorFilter(matrix);
    }
    
    // Colores de tipos Pokémon
    private static final Map<String, Integer> TYPE_COLORS = new HashMap<>();
    static {
        TYPE_COLORS.put("Normal", Color.parseColor("#A8A878"));
        TYPE_COLORS.put("Fuego", Color.parseColor("#F08030"));
        TYPE_COLORS.put("Fire", Color.parseColor("#F08030"));
        TYPE_COLORS.put("Agua", Color.parseColor("#6890F0"));
        TYPE_COLORS.put("Water", Color.parseColor("#6890F0"));
        TYPE_COLORS.put("Planta", Color.parseColor("#78C850"));
        TYPE_COLORS.put("Grass", Color.parseColor("#78C850"));
        TYPE_COLORS.put("Eléctrico", Color.parseColor("#F8D030"));
        TYPE_COLORS.put("Electric", Color.parseColor("#F8D030"));
        TYPE_COLORS.put("Hielo", Color.parseColor("#98D8D8"));
        TYPE_COLORS.put("Ice", Color.parseColor("#98D8D8"));
        TYPE_COLORS.put("Lucha", Color.parseColor("#C03028"));
        TYPE_COLORS.put("Fighting", Color.parseColor("#C03028"));
        TYPE_COLORS.put("Veneno", Color.parseColor("#A040A0"));
        TYPE_COLORS.put("Poison", Color.parseColor("#A040A0"));
        TYPE_COLORS.put("Tierra", Color.parseColor("#E0C068"));
        TYPE_COLORS.put("Ground", Color.parseColor("#E0C068"));
        TYPE_COLORS.put("Volador", Color.parseColor("#A890F0"));
        TYPE_COLORS.put("Flying", Color.parseColor("#A890F0"));
        TYPE_COLORS.put("Psíquico", Color.parseColor("#F85888"));
        TYPE_COLORS.put("Psychic", Color.parseColor("#F85888"));
        TYPE_COLORS.put("Bicho", Color.parseColor("#A8B820"));
        TYPE_COLORS.put("Bug", Color.parseColor("#A8B820"));
        TYPE_COLORS.put("Roca", Color.parseColor("#B8A038"));
        TYPE_COLORS.put("Rock", Color.parseColor("#B8A038"));
        TYPE_COLORS.put("Fantasma", Color.parseColor("#705898"));
        TYPE_COLORS.put("Ghost", Color.parseColor("#705898"));
        TYPE_COLORS.put("Dragón", Color.parseColor("#7038F8"));
        TYPE_COLORS.put("Dragon", Color.parseColor("#7038F8"));
        TYPE_COLORS.put("Hada", Color.parseColor("#EE99AC"));
        TYPE_COLORS.put("Fairy", Color.parseColor("#EE99AC"));
    }
    
    public PokedexAdapter(UserSettingsRepository settingsRepository) {
        super(new PokedexDiffCallback());
        this.settingsRepository = settingsRepository;
    }
    
    @NonNull
    @Override
    public PokedexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_pokedex, parent, false);
        return new PokedexViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PokedexViewHolder holder, int position) {
        PokedexEntryEntity entry = getItem(position);
        holder.bind(entry, settingsRepository, GRAYSCALE_FILTER, TYPE_COLORS);
    }
    
    static class PokedexViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardContainer;
        private final TextView tvNumber;
        private final TextView tvName;
        private final TextView tvTypes;
        private final TextView tvDescription;
        private final ImageView ivPokemonImage;
        private final ImageView ivUnlockIndicator;
        private final View viewImageBg;
        
        public PokedexViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_pokedex_item);
            tvNumber = itemView.findViewById(R.id.tv_pokedex_number);
            tvName = itemView.findViewById(R.id.tv_pokedex_name);
            tvTypes = itemView.findViewById(R.id.tv_pokedex_types);
            tvDescription = itemView.findViewById(R.id.tv_pokedex_description);
            ivPokemonImage = itemView.findViewById(R.id.iv_pokemon_image);
            ivUnlockIndicator = itemView.findViewById(R.id.iv_unlock_indicator);
            viewImageBg = itemView.findViewById(R.id.view_image_bg);
        }
        
        public void bind(PokedexEntryEntity entry, UserSettingsRepository settingsRepository, 
                        ColorMatrixColorFilter grayscaleFilter, Map<String, Integer> typeColors) {
            
            // Número de Pokédex - siempre visible
            tvNumber.setText(String.format("#%03d", entry.pokedexNumber));
            
            String language = getLanguage(settingsRepository);
            String name = "en".equals(language) ? entry.nameEn : entry.name;
            tvName.setText(name != null ? name : entry.name);
            
            // Descripción
            String description = "en".equals(language) ? entry.descriptionEn : entry.description;
            tvDescription.setText(description != null ? description : entry.description);
            
            // Cargar imagen
            loadPokemonImage(entry);
            
            // Configurar tipos con colores
            setupTypeColors(entry, typeColors, entry.isUnlocked);
            
            // Aplicar estilo según estado
            applyStyle(entry.isUnlocked, grayscaleFilter);
        }
        
        /**
         * Configura los colores de los tipos
         */
        private void setupTypeColors(PokedexEntryEntity entry, Map<String, Integer> typeColors, boolean isUnlocked) {
            String type1 = entry.type1;
            String type2 = entry.type2;
            
            // Obtener color del tipo principal
            int type1Color = typeColors.getOrDefault(type1, Color.parseColor("#888888"));
            
            if (isUnlocked) {
                // Si está desbloqueado, mostrar colores vivos
                if (type2 != null && !type2.isEmpty()) {
                    int type2Color = typeColors.getOrDefault(type2, type1Color);
                    tvTypes.setText(type1 + " / " + type2);
                    // Usar el color del primer tipo para el fondo
                    setTypeBackground(tvTypes, type1Color);
                } else {
                    tvTypes.setText(type1);
                    setTypeBackground(tvTypes, type1Color);
                }
                tvTypes.setTextColor(Color.WHITE);
            } else {
                // Si está bloqueado, mostrar en gris
                if (type2 != null && !type2.isEmpty()) {
                    tvTypes.setText(type1 + " / " + type2);
                } else {
                    tvTypes.setText(type1);
                }
                setTypeBackground(tvTypes, Color.parseColor("#888888"));
                tvTypes.setTextColor(Color.parseColor("#DDDDDD"));
            }
        }
        
        /**
         * Aplica un fondo redondeado al TextView de tipo
         */
        private void setTypeBackground(TextView textView, int color) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(16);
            drawable.setColor(color);
            drawable.setPadding(16, 8, 16, 8);
            textView.setBackground(drawable);
            textView.setPadding(24, 8, 24, 8);
        }
        
        private String getLanguage(UserSettingsRepository settingsRepository) {
            try {
                var settings = settingsRepository.getSettingsSync();
                return settings != null ? settings.language : "es";
            } catch (Exception e) {
                return "es";
            }
        }
        
        private void loadPokemonImage(PokedexEntryEntity entry) {
            String imageUrl = POKEMON_SPRITE_URL + entry.pokedexNumber + ".png";
            
            Glide.with(itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_pokemon_placeholder)
                .error(R.drawable.ic_pokemon_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivPokemonImage);
        }
        
        /**
         * Aplica el estilo visual según si el Pokémon está desbloqueado o no.
         */
        private void applyStyle(boolean isUnlocked, ColorMatrixColorFilter grayscaleFilter) {
            
            if (isUnlocked) {
                // === DESBLOQUEADO ===
                cardContainer.setCardBackgroundColor(Color.WHITE);
                cardContainer.setAlpha(1.0f);
                cardContainer.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.masterball_pink));
                cardContainer.setStrokeWidth(4);
                
                ivPokemonImage.setColorFilter(null);
                ivPokemonImage.setAlpha(1.0f);
                
                if (viewImageBg != null) viewImageBg.setAlpha(1.0f);
                if (ivUnlockIndicator != null) ivUnlockIndicator.setVisibility(View.VISIBLE);
                
                // Número siempre visible con fondo
                tvNumber.setTextColor(Color.WHITE);
                setNumberBackground(tvNumber, ContextCompat.getColor(itemView.getContext(), R.color.masterball_purple));
                
                tvName.setTextColor(Color.parseColor("#1A1A1A"));
                tvDescription.setTextColor(Color.parseColor("#666666"));
                
            } else {
                // === BLOQUEADO ===
                cardContainer.setCardBackgroundColor(Color.parseColor("#D0D0D0"));
                cardContainer.setAlpha(1.0f);
                cardContainer.setStrokeColor(Color.parseColor("#AAAAAA"));
                cardContainer.setStrokeWidth(1);
                
                ivPokemonImage.setColorFilter(grayscaleFilter);
                ivPokemonImage.setAlpha(0.5f);
                
                if (viewImageBg != null) viewImageBg.setAlpha(0.3f);
                if (ivUnlockIndicator != null) ivUnlockIndicator.setVisibility(View.GONE);
                
                // Número siempre visible con fondo gris oscuro
                tvNumber.setTextColor(Color.WHITE);
                setNumberBackground(tvNumber, Color.parseColor("#666666"));
                
                tvName.setTextColor(Color.parseColor("#555555"));
                tvDescription.setTextColor(Color.parseColor("#777777"));
            }
        }
        
        /**
         * Aplica un fondo redondeado al número de Pokédex
         */
        private void setNumberBackground(TextView textView, int color) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(12);
            drawable.setColor(color);
            textView.setBackground(drawable);
            textView.setPadding(20, 6, 20, 6);
        }
    }
    
    private static class PokedexDiffCallback extends DiffUtil.ItemCallback<PokedexEntryEntity> {
        @Override
        public boolean areItemsTheSame(@NonNull PokedexEntryEntity oldItem, 
                                       @NonNull PokedexEntryEntity newItem) {
            return oldItem.pokedexNumber == newItem.pokedexNumber;
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull PokedexEntryEntity oldItem, 
                                          @NonNull PokedexEntryEntity newItem) {
            return oldItem.isUnlocked == newItem.isUnlocked;
        }
    }
}
