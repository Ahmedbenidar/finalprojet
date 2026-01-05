package com.example.costumes.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.costumes.R;
import com.example.costumes.models.Costume;

import java.util.ArrayList;
import java.util.List;

public class CostumeAdapter extends RecyclerView.Adapter<CostumeAdapter.CostumeViewHolder> {

    private static final String TAG = "CostumeAdapter";
    private List<Costume> costumes;
    private Context context;
    private Handler mainHandler;

    // Garder une référence aux URLs chargées
    private List<String> loadedUrls = new ArrayList<>();

    // URL de base pour les images
    private static final String BASE_URL = "http://10.0.2.2:8000";
    private static final String IMAGE_BASE_URL = BASE_URL + "/storage/image/";

    // Dimensions fixes pour les images
    private static final int IMAGE_WIDTH = 600;
    private static final int IMAGE_HEIGHT = 400;
    private static final int CORNER_RADIUS = 12;

    public CostumeAdapter(Context context) {
        this.context = context;
        this.costumes = new ArrayList<>();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @NonNull
    @Override
    public CostumeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_costume, parent, false);
        return new CostumeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CostumeViewHolder holder, int position) {
        if (costumes.isEmpty() || position >= costumes.size()) {
            return;
        }

        Costume costume = costumes.get(position);

        Log.d(TAG, "Binding position " + position + ": " + costume.getNom());

        // Afficher les informations textuelles
        holder.nomTextView.setText(costume.getNom());
        holder.typeTextView.setText(costume.getType() != null ? costume.getType() : "Non spécifié");
        holder.prixTextView.setText(String.format("%.2f €", costume.getPrix()));

        // Gérer la disponibilité
        if (costume.isDisponibilite()) {
            holder.disponibiliteTextView.setText("Disponible");
            holder.disponibiliteTextView.setBackgroundResource(R.drawable.bg_rounded_green);
            holder.disponibiliteTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        } else {
            holder.disponibiliteTextView.setText("Indisponible");
            holder.disponibiliteTextView.setBackgroundResource(R.drawable.bg_rounded_red);
            holder.disponibiliteTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        }

        // Réinitialiser l'image
        holder.imageView.setImageResource(R.drawable.placeholder_costume);

        // Mettre un tag pour éviter les chargements incorrects
        holder.imageView.setTag(position);

        // Charger l'image
        loadImageWithGlide(holder.imageView, costume, position);
    }

    @Override
    public int getItemCount() {
        return costumes.size();
    }

    @Override
    public void onViewRecycled(@NonNull CostumeViewHolder holder) {
        super.onViewRecycled(holder);
        // Empêcher Glide d'annuler le chargement
        Glide.with(context).clear(holder.imageView);
    }

    public void setCostumes(List<Costume> newCostumes) {
        Log.d(TAG, "=== setCostumes() ===");

        if (newCostumes == null) {
            this.costumes.clear();
        } else {
            this.costumes = new ArrayList<>(newCostumes);

            // Log détaillé
            for (int i = 0; i < newCostumes.size(); i++) {
                Costume c = newCostumes.get(i);
                Log.d(TAG, String.format(
                        "[%d] '%s' | Image: '%s'",
                        i, c.getNom(), c.getImage_path()
                ));
            }
        }

        // Réinitialiser les URLs chargées
        loadedUrls.clear();

        notifyDataSetChanged();
    }

    /**
     * Charger l'image avec Glide - VERSION AMÉLIORÉE
     */
    private void loadImageWithGlide(ImageView imageView, Costume costume, int position) {
        String imagePath = costume.getImage_path();

        if (imagePath == null || imagePath.trim().isEmpty()) {
            Log.w(TAG, "⚠️ [" + position + "] Pas d'image pour: " + costume.getNom());
            imageView.setImageResource(R.drawable.error_costume);
            return;
        }

        // Nettoyer le chemin
        imagePath = imagePath.trim();

        // LOG DÉTAILLÉ
        Log.d(TAG, "=== DEBUG IMAGE [" + position + "] ===");
        Log.d(TAG, "Nom: " + costume.getNom());
        Log.d(TAG, "Chemin original: '" + imagePath + "'");

        // ESSAYER DIFFÉRENTS FORMATS D'URL
        List<String> possibleUrls = generatePossibleUrls(imagePath);

        for (int i = 0; i < possibleUrls.size(); i++) {
            Log.d(TAG, "URL possible [" + i + "]: " + possibleUrls.get(i));
        }

        // Charger avec la première URL, les autres seront essayées en cas d'échec
        loadWithSingleUrl(imageView, costume, position, possibleUrls.get(0));
    }

    private List<String> generatePossibleUrls(String imagePath) {
        List<String> urls = new ArrayList<>();

        // Nettoyer le chemin
        imagePath = imagePath.replace("\\", "/").trim();

        // Extraire le nom de fichier
        String filename;
        if (imagePath.contains("/")) {
            filename = imagePath.substring(imagePath.lastIndexOf("/") + 1);
        } else {
            filename = imagePath;
        }

        filename = filename.trim();

        // 1. URL originale (sans modification)
        urls.add(IMAGE_BASE_URL + filename);

        // 2. Avec différentes extensions
        if (!filename.contains(".")) {
            urls.add(IMAGE_BASE_URL + filename + ".jpg");
            urls.add(IMAGE_BASE_URL + filename + ".jpeg");
            urls.add(IMAGE_BASE_URL + filename + ".png");
            urls.add(IMAGE_BASE_URL + filename + ".webp");
        } else {
            // Si déjà une extension, essayer sans aussi
            String nameWithoutExt = filename.substring(0, filename.lastIndexOf('.'));
            urls.add(IMAGE_BASE_URL + nameWithoutExt + ".jpg");
            urls.add(IMAGE_BASE_URL + nameWithoutExt + ".png");
        }

        // 3. URL complète si c'est déjà une URL
        if (imagePath.startsWith("http")) {
            urls.add(imagePath);
        }

        // 4. URL relative au storage Laravel
        if (imagePath.contains("storage/")) {
            String relativePath = imagePath.substring(imagePath.indexOf("storage/"));
            urls.add(BASE_URL + "/" + relativePath);
        }

        // 5. Enlever les espaces et caractères spéciaux
        String cleanFilename = filename.replace(" ", "%20")
                .replace("'", "")
                .replace("\"", "");
        urls.add(IMAGE_BASE_URL + cleanFilename);

        return urls;
    }

    private void loadWithSingleUrl(ImageView imageView, Costume costume, int position, String imageUrl) {
        // Vérifier si déjà chargée
        if (loadedUrls.contains(imageUrl)) {
            Log.d(TAG, "Image déjà chargée: " + costume.getNom());
            return;
        }

        Log.d(TAG, "Chargement [" + position + "]: " + imageUrl);

        try {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.placeholder_costume)
                    .error(R.drawable.error_costume)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
                    .override(IMAGE_WIDTH, IMAGE_HEIGHT)
                    .centerCrop();

            Glide.with(context)
                    .load(imageUrl)
                    .apply(requestOptions)
                    .transform(new CenterCrop(), new RoundedCorners(CORNER_RADIUS))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e,
                                                    Object model,
                                                    Target<Drawable> target,
                                                    boolean isFirstResource) {
                            Log.e(TAG, "❌ Échec [" + position + "]: " + imageUrl);
                            loadedUrls.remove(imageUrl);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource,
                                                       Object model,
                                                       Target<Drawable> target,
                                                       DataSource dataSource,
                                                       boolean isFirstResource) {
                            Log.d(TAG, "✅ Succès [" + position + "]: " + costume.getNom());
                            loadedUrls.add(imageUrl);
                            return false;
                        }
                    })
                    .into(imageView);

        } catch (Exception e) {
            Log.e(TAG, "Exception Glide [" + position + "]: " + e.getMessage());
            imageView.setImageResource(R.drawable.error_costume);
        }
    }

    /**
     * PRÉCHARGER TOUTES les images
     */
    public void preloadAllImages() {
        Log.d(TAG, "Préchargement de TOUTES les images");

        if (costumes == null || costumes.isEmpty()) {
            return;
        }

        for (int i = 0; i < costumes.size(); i++) {
            final int position = i;
            final Costume costume = costumes.get(i);
            String imagePath = costume.getImage_path();

            if (imagePath != null && !imagePath.trim().isEmpty()) {
                mainHandler.postDelayed(() -> {
                    List<String> urls = generatePossibleUrls(imagePath);
                    if (!urls.isEmpty()) {
                        String url = urls.get(0);

                        Glide.with(context)
                                .load(url)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .preload();

                        Log.d(TAG, "Préchargé [" + position + "]: " + costume.getNom());
                    }
                }, i * 100); // Délai progressif pour éviter la surcharge
            }
        }
    }

    /**
     * Rafraîchir toutes les images
     */
    public void refreshAllImages() {
        loadedUrls.clear();
        notifyDataSetChanged();
    }

    /**
     * ViewHolder
     */
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