package com.example.costumes.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.costumes.R;
import com.example.costumes.models.Costume;

import java.util.ArrayList;
import java.util.List;

public class CostumeAdapter extends RecyclerView.Adapter<CostumeAdapter.CostumeViewHolder> {

    private List<Costume> costumes;
    private Context context;

    public CostumeAdapter(Context context) {
        this.context = context;
        this.costumes = new ArrayList<>();
    }

    @NonNull
    @Override
    public CostumeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_costume, parent, false);
        return new CostumeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CostumeViewHolder holder, int position) {
        Costume costume = costumes.get(position);

        holder.nomTextView.setText(costume.getNom());
        holder.typeTextView.setText(costume.getType() != null ? costume.getType() : "Non spécifié");
        holder.prixTextView.setText(String.format("%.2f €", costume.getPrix()));

        if (costume.isDisponibilite()) {
            holder.disponibiliteTextView.setText("Disponible");
            holder.disponibiliteTextView.setBackgroundResource(R.drawable.bg_rounded_green);
            holder.disponibiliteTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        } else {
            holder.disponibiliteTextView.setText("Indisponible");
            holder.disponibiliteTextView.setBackgroundResource(R.drawable.bg_rounded_red);
            holder.disponibiliteTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        }

        // CHARGEMENT DES IMAGES - SOLUTION SIMPLE
        loadImageSimple(holder.imageView, costume);
    }

    @Override
    public int getItemCount() {
        return costumes.size();
    }

    public void setCostumes(List<Costume> newCostumes) {
        this.costumes.clear();
        if (newCostumes != null) {
            this.costumes.addAll(newCostumes);
        }
        notifyDataSetChanged();
        Log.d("CostumeAdapter", "Données mises à jour: " + this.costumes.size() + " costumes");
    }

    /**
     * Méthode simple pour charger les images
     */
    private void loadImageSimple(ImageView imageView, Costume costume) {
        String imagePath = costume.getImage_path();

        if (imagePath == null || imagePath.isEmpty()) {
            Log.d("CostumeAdapter", "Pas d'image pour: " + costume.getNom());
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
            return;
        }

        Log.d("CostumeAdapter", "Image pour '" + costume.getNom() + "': " + imagePath);

        // Construire l'URL FINALE
        String imageUrl = buildFinalUrl(imagePath);
        Log.d("CostumeAdapter", "URL finale: " + imageUrl);

        // CONFIGURATION CRITIQUE POUR HTTP LOCAL
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // IMPORTANT: pas de cache
                .skipMemoryCache(true)                     // IMPORTANT: pas de cache mémoire
                .timeout(10000)                           // Timeout 10 secondes
                .centerCrop();

        try {
            Glide.with(context)
                    .load(imageUrl)
                    .apply(requestOptions)
                    .into(imageView);
        } catch (Exception e) {
            Log.e("CostumeAdapter", "Erreur: " + e.getMessage());
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    /**
     * Construire l'URL finale pour Laravel
     */
    private String buildFinalUrl(String imagePath) {
        // Nettoyer le chemin
        if (imagePath.startsWith("storage/")) {
            imagePath = imagePath.substring(8); // Enlève "storage/"
        }
        if (imagePath.startsWith("public/")) {
            imagePath = imagePath.substring(7); // Enlève "public/"
        }

        // Ajouter "image/" si pas déjà présent
        if (!imagePath.startsWith("image/")) {
            imagePath = "image/" + imagePath;
        }

        // URL finale
        return "http://10.0.2.2:8000/storage/" + imagePath;
    }

    static class CostumeViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageView;
        TextView nomTextView;
        TextView typeTextView;
        TextView prixTextView;
        TextView disponibiliteTextView;

        CostumeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageView);
            nomTextView = itemView.findViewById(R.id.nomTextView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            prixTextView = itemView.findViewById(R.id.prixTextView);
            disponibiliteTextView = itemView.findViewById(R.id.disponibiliteTextView);
        }
    }
}