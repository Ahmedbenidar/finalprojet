package com.example.costumes;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.costumes.adapters.CostumeAdapter;
import com.example.costumes.models.Costume;
import com.example.costumes.services.ApiService;
import com.example.costumes.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.List;

public class CostumesListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private CostumeAdapter adapter;
    private List<Costume> costumesList;
    private ApiService apiService;
    private SharedPreferencesManager prefsManager;
    private Handler uiHandler;

    private static final String TAG = "CostumesListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_costumes_list);

        Log.d(TAG, "=== DÉMARRAGE ACTIVITÉ ===");
        uiHandler = new Handler(Looper.getMainLooper());

        // VÉRIFICATION DE CONNEXION RENFORCÉE
        prefsManager = new SharedPreferencesManager(this);
        if (!prefsManager.isLoggedIn()) {
            Log.w(TAG, "❌ NON CONNECTÉ - Redirection vers Login");
            Toast.makeText(this, "Session expirée, veuillez vous reconnecter", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        Log.d(TAG, "✅ Connecté en tant que: " + prefsManager.getUserEmail());

        // CONFIGURER LA BARRE D'ACTION
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Costumes - " + prefsManager.getUserName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            Log.d(TAG, "ActionBar configurée");
        }

        apiService = new ApiService();
        costumesList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        adapter = new CostumeAdapter(this);
        Log.d(TAG, "Adapter créé");

        // Configuration optimisée de la RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setItemPrefetchEnabled(true);
        layoutManager.setInitialPrefetchItemCount(10);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // Charger les costumes
        loadCostumes();

        // ==================== GESTION MODERNE DU BOUTON RETOUR ====================
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "Bouton retour pressé");
                showExitConfirmationDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - Connecté: " + prefsManager.isLoggedIn());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy - Activité détruite");
    }

    private void loadCostumes() {
        Log.d(TAG, "=== DEBUT loadCostumes ===");

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        // Afficher un message de chargement
        emptyView.setText("Chargement des costumes...");
        emptyView.setVisibility(View.VISIBLE);

        apiService.getCostumes(new ApiService.CostumesCallback() {
            @Override
            public void onSuccess(List<Costume> costumes) {
                Log.d(TAG, "✅ Réponse API reçue - " + costumes.size() + " costumes");

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    if (costumes != null && !costumes.isEmpty()) {
                        // LOG DÉTAILLÉ POUR DÉBOGUER
                        Log.d(TAG, "=== DÉTAILS DES COSTUMES ===");
                        for (int i = 0; i < costumes.size(); i++) {
                            Costume c = costumes.get(i);
                            Log.d(TAG, String.format(
                                    "[%d] %s | Image: '%s' | Prix: %.2f | Dispo: %s",
                                    i, c.getNom(), c.getImage_path(), c.getPrix(), c.isDisponibilite()
                            ));
                        }

                        costumesList.clear();
                        costumesList.addAll(costumes);

                        // 1. Mettre à jour l'adapter IMMÉDIATEMENT
                        adapter.setCostumes(costumesList);

                        // 2. Afficher la RecyclerView IMMÉDIATEMENT (avec placeholders)
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);

                        // 3. FORCER LE PRÉCHARGEMENT DE TOUTES LES IMAGES
                        uiHandler.postDelayed(() -> {
                            Log.d(TAG, "Début du préchargement de TOUTES les images");
                            adapter.preloadAllImages();

                            // 4. Rafraîchir après 2 secondes pour afficher toutes les images
                            uiHandler.postDelayed(() -> {
                                Log.d(TAG, "Rafraîchissement complet des images");
                                adapter.refreshAllImages();

                                // Vérifier combien d'images sont chargées
                                checkLoadedImages();
                            }, 2000); // 2 secondes pour laisser le temps de tout précharger
                        }, 300); // Petit délai avant de commencer

                        // 5. Message de succès
                        Toast.makeText(CostumesListActivity.this,
                                costumes.size() + " costumes chargés",
                                Toast.LENGTH_SHORT).show();

                        Log.d(TAG, "Liste affichée - Préchargement en cours...");

                    } else {
                        Log.w(TAG, "Liste vide ou null reçue de l'API");
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                        emptyView.setText("Aucun costume disponible");
                        Toast.makeText(CostumesListActivity.this,
                                "Aucun costume trouvé", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Erreur API: " + error);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText("Erreur de chargement\n" + error);

                    Toast.makeText(CostumesListActivity.this,
                            "Erreur: " + error,
                            Toast.LENGTH_LONG).show();

                    // Log supplémentaire
                    Log.e(TAG, "Vérifiez que le serveur Laravel est en cours d'exécution");
                    Log.e(TAG, "URL API: http://10.0.2.2:8000/api/costumes");
                });
            }
        });
    }

    /**
     * Vérifier combien d'images sont réellement chargées
     */
    private void checkLoadedImages() {
        uiHandler.postDelayed(() -> {
            int totalCostumes = costumesList.size();
            int imagesWithPath = 0;

            for (Costume costume : costumesList) {
                if (costume.getImage_path() != null && !costume.getImage_path().trim().isEmpty()) {
                    imagesWithPath++;
                }
            }

            Log.d(TAG, "=== STATISTIQUES IMAGES ===");
            Log.d(TAG, "Total costumes: " + totalCostumes);
            Log.d(TAG, "Costumes avec chemin image: " + imagesWithPath);
            Log.d(TAG, "Costumes sans image: " + (totalCostumes - imagesWithPath));

            if (imagesWithPath < totalCostumes) {
                Toast.makeText(this,
                        imagesWithPath + "/" + totalCostumes + " images chargées",
                        Toast.LENGTH_SHORT).show();
            }
        }, 1000);
    }

    // ==================== MENU (TROIS POINTS) ====================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "Création menu options");

        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem refreshItem = menu.findItem(R.id.action_refresh);
        MenuItem logoutItem = menu.findItem(R.id.action_logout);

        if (refreshItem != null && logoutItem != null) {
            Log.d(TAG, "✅ Menu items trouvés");
        } else {
            Log.e(TAG, "❌ Menu items NON trouvés!");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Log.d(TAG, "Option sélectionnée: " + item.getTitle());

        if (id == R.id.action_refresh) {
            // Rafraîchir la liste
            Log.d(TAG, "Action: Rafraîchir la liste");
            Toast.makeText(this, "Rafraîchissement en cours...", Toast.LENGTH_SHORT).show();

            // Vider le cache de Glide
            clearGlideCache();

            // Recharger après un court délai
            uiHandler.postDelayed(this::loadCostumes, 300);
            return true;

        } else if (id == R.id.action_logout) {
            Log.d(TAG, "Action: Déconnexion");
            showLogoutDialog();
            return true;

        } else if (id == android.R.id.home) {
            Log.d(TAG, "Action: Bouton retour (home)");
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Vider le cache de Glide
     */
    private void clearGlideCache() {
        new Thread(() -> {
            com.bumptech.glide.Glide.get(getApplicationContext()).clearDiskCache();
            uiHandler.post(() -> {
                com.bumptech.glide.Glide.get(getApplicationContext()).clearMemory();
                Toast.makeText(this, "Cache vidé", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Cache Glide vidé");
            });
        }).start();
    }

    private void showLogoutDialog() {
        Log.d(TAG, "Affichage dialogue de déconnexion");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        TextView message = new TextView(this);
        message.setText("Êtes-vous sûr de vouloir vous déconnecter ?");
        message.setTextSize(18);
        message.setTextColor(Color.WHITE);
        message.setPadding(40, 30, 40, 20);
        message.setGravity(Gravity.CENTER);
        message.setBackgroundColor(getResources().getColor(R.color.purple_500));

        builder.setTitle("Déconnexion")
                .setView(message)
                .setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Utilisateur a confirmé la déconnexion");
                        performLogout();
                    }
                })
                .setNegativeButton("NON", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Utilisateur a annulé la déconnexion");
                        dialog.dismiss();
                    }
                })
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getWindow().setBackgroundDrawableResource(R.color.purple_500);

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (positiveButton != null) {
            positiveButton.setTextColor(Color.WHITE);
            positiveButton.setBackgroundColor(getResources().getColor(R.color.purple_700));
            positiveButton.setPadding(20, 10, 20, 10);
        }

        if (negativeButton != null) {
            negativeButton.setTextColor(Color.WHITE);
            negativeButton.setBackgroundColor(getResources().getColor(R.color.purple_700));
            negativeButton.setPadding(20, 10, 20, 10);
        }

        TextView titleView = dialog.findViewById(android.R.id.title);
        if (titleView != null) {
            titleView.setTextColor(Color.WHITE);
        }
    }

    private void showExitConfirmationDialog() {
        Log.d(TAG, "Affichage dialogue de confirmation de sortie");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Quitter l'application")
                .setMessage("Voulez-vous vraiment quitter l'application ?")
                .setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Utilisateur a confirmé la sortie");
                        finishAffinity();
                    }
                })
                .setNegativeButton("NON", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Utilisateur a annulé la sortie");
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .create();

        dialog.show();

        dialog.getWindow().setBackgroundDrawableResource(R.color.purple_500);

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (positiveButton != null) {
            positiveButton.setTextColor(Color.WHITE);
            positiveButton.setBackgroundColor(getResources().getColor(R.color.purple_700));
        }

        if (negativeButton != null) {
            negativeButton.setTextColor(Color.WHITE);
            negativeButton.setBackgroundColor(getResources().getColor(R.color.purple_700));
        }

        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setTextColor(Color.WHITE);
        }

        TextView titleView = dialog.findViewById(android.R.id.title);
        if (titleView != null) {
            titleView.setTextColor(Color.WHITE);
        }
    }

    private void performLogout() {
        Log.d(TAG, "=== DÉBUT DÉCONNEXION ===");

        prefsManager.logout();

        Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "Redirection vers LoginActivity");
        Intent intent = new Intent(CostumesListActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Log.d(TAG, "=== FIN DÉCONNEXION ===");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause - Activité en pause");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart - Activité démarrée");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop - Activité stoppée");
    }

    /**
     * MÉTHODE POUR FORCER LE RECHARGEMENT DES IMAGES
     * À appeler via un bouton debug si nécessaire
     */
    public void forceReloadImages(View view) {
        Toast.makeText(this, "Forcer le rechargement des images...", Toast.LENGTH_SHORT).show();

        if (adapter != null) {
            clearGlideCache();

            uiHandler.postDelayed(() -> {
                adapter.refreshAllImages();
                Toast.makeText(this, "Images rechargées", Toast.LENGTH_SHORT).show();
            }, 500);
        }
    }
}