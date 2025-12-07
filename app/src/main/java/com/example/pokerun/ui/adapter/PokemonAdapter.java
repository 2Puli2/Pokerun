package com.example.pokerun.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.pokerun.R;
import com.example.pokerun.data.database.entity.PokemonEntity;

public class PokemonAdapter extends ListAdapter<PokemonEntity, PokemonAdapter.PokemonViewHolder> {
    private OnPokemonClickListener listener;
    
    // URL base para sprites de Pokémon
    private static final String POKEMON_SPRITE_URL = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/";
    
    public interface OnPokemonClickListener {
        void onPokemonClick(PokemonEntity pokemon);
    }
    
    public PokemonAdapter(OnPokemonClickListener listener) {
        super(new DiffUtil.ItemCallback<PokemonEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull PokemonEntity oldItem, @NonNull PokemonEntity newItem) {
                return oldItem.id == newItem.id;
            }
            
            @Override
            public boolean areContentsTheSame(@NonNull PokemonEntity oldItem, @NonNull PokemonEntity newItem) {
                return oldItem.pokedexNumber == newItem.pokedexNumber &&
                       java.util.Objects.equals(oldItem.name, newItem.name) &&
                       oldItem.evolvesTo == newItem.evolvesTo;
            }
        });
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public PokemonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pokemon, parent, false);
        return new PokemonViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PokemonViewHolder holder, int position) {
        PokemonEntity pokemon = getItem(position);
        holder.bind(pokemon, listener);
    }
    
    static class PokemonViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNumber, tvName, tvTypes;
        private ImageView ivPokemonImage;
        private Button btnEvolve;
        
        public PokemonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tv_pokemon_number);
            tvName = itemView.findViewById(R.id.tv_pokemon_name);
            tvTypes = itemView.findViewById(R.id.tv_pokemon_types);
            ivPokemonImage = itemView.findViewById(R.id.iv_pokemon_image);
            btnEvolve = itemView.findViewById(R.id.btn_evolve);
        }
        
        public void bind(PokemonEntity pokemon, OnPokemonClickListener listener) {
            tvNumber.setText("#" + String.format("%03d", pokemon.pokedexNumber));
            tvName.setText(pokemon.name);
            
            String types = pokemon.type1;
            if (pokemon.type2 != null && !pokemon.type2.isEmpty()) {
                types += " / " + pokemon.type2;
            }
            tvTypes.setText(types);
            
            // Cargar imagen del Pokémon
            if (ivPokemonImage != null) {
                String imageUrl = POKEMON_SPRITE_URL + pokemon.pokedexNumber + ".png";
                
                RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.ic_pokemon_placeholder)
                    .error(R.drawable.ic_pokemon_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
                
                Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .apply(options)
                    .into(ivPokemonImage);
            }
            
            if (pokemon.evolvesTo > 0) {
                btnEvolve.setVisibility(View.VISIBLE);
                btnEvolve.setOnClickListener(v -> listener.onPokemonClick(pokemon));
            } else {
                btnEvolve.setVisibility(View.GONE);
            }
        }
    }
}

