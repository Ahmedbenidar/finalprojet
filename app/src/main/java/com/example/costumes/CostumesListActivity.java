package com.example.costumes;
import android.graphics.Color;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_costumes_list);

        Log.d("CostumesListActivity", "=== DÉMARRAGE ===");

        // VÉRIFICATION DE CONNEXION RENFORCÉE
        prefsManager = new SharedPreferencesManager(this);
        if (!prefsManager.isLoggedIn()) {
            Log.w("CostumesListActivity", "❌ NON CONNECTÉ - Redirection vers Login");
            Toast.makeText(this, "Session expirée, veuillez vous reconnecter", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        Log.d("CostumesListActivity", "✅ Connecté en tant que: " + prefsManager.getUserEmail());

        // CONFIGURER LA BARRE D'ACTION
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Costumes - " + prefsManager.getUserName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(false); // Pas de flèche retour
            // La barre sera automatiquement purple grâce à votre thème
        }

        apiService = new ApiService();
        costumesList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        adapter = new CostumeAdapter(this);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        loadCostumes();

        // ==================== GESTION MODERNE DU BOUTON RETOUR ====================
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Demander confirmation avant de quitter
                showExitConfirmationDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("CostumesListActivity", "onResume - Connecté: " + prefsManager.isLoggedIn());
    }

    private void loadCostumes() {
        Log.d("CostumesListActivity", "Chargement des costumes...");

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        apiService.getCostumes(new ApiService.CostumesCallback() {
            @Override
            public void onSuccess(List<Costume> costumes) {
                runOnUiThread(() -> {
                    Log.d("CostumesListActivity", "✅ Costumes reçus: " + (costumes != null ? costumes.size() : 0));
                    progressBar.setVisibility(View.GONE);

                    if (costumes != null && !costumes.isEmpty()) {
                        costumesList.clear();
                        costumesList.addAll(costumes);
                        adapter.setCostumes(costumesList);
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);

                        Toast.makeText(CostumesListActivity.this,
                                costumes.size() + " costumes disponibles",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                        emptyView.setText("Aucun costume disponible");
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e("CostumesListActivity", "❌ Erreur: " + error);
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText("Erreur de chargement\n" + error);
                });
            }
        });
    }

    // ==================== MENU (TROIS POINTS) ====================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflater le menu - cela va ajouter les trois points en haut à droite
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Vérifier que le menu contient bien l'item de déconnexion
        MenuItem logoutItem = menu.findItem(R.id.action_logout);
        if (logoutItem != null) {
            Log.d("CostumesListActivity", "✅ Menu item déconnexion trouvé");
        } else {
            Log.e("CostumesListActivity", "❌ Menu item déconnexion NON trouvé!");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            // Rafraîchir la liste
            Log.d("CostumesListActivity", "Action: Rafraîchir");
            Toast.makeText(this, "Rafraîchissement en cours...", Toast.LENGTH_SHORT).show();
            loadCostumes();
            return true;

        } else if (id == R.id.action_logout) {
            // Déconnexion
            Log.d("CostumesListActivity", "Action: Déconnexion");
            showLogoutDialog();
            return true;

        } else if (id == android.R.id.home) {
            // Gestion de la flèche de retour (si activée)
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_CostumesApp_Dialog);

        TextView message = new TextView(this);
        message.setText("Êtes-vous sûr de vouloir vous déconnecter ?");
        message.setTextSize(18);
        message.setTextColor(Color.WHITE);
        message.setPadding(40, 30, 40, 20);
        message.setGravity(Gravity.CENTER);

        // Fond violet pour le message
        message.setBackgroundColor(getResources().getColor(R.color.purple_700));

        builder.setTitle("Déconnexion")
                .setView(message)
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performLogout();
                    }
                })
                .setNegativeButton("Non", null)
                .setIcon(android.R.drawable.ic_dialog_alert);

        AlertDialog dialog = builder.create();
        dialog.show();

        // CHANGER LE FOND DE TOUTE LA FENÊTRE
        dialog.getWindow().setBackgroundDrawableResource(R.color.purple_700);

        // Optionnel: changer aussi la couleur du titre
        TextView titleView = dialog.findViewById(android.R.id.title);
        if (titleView != null) {
            titleView.setTextColor(Color.WHITE);
        }

        // Personnaliser les boutons
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);

        // Fond violet pour les boutons aussi
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(
                getResources().getColor(R.color.purple_700));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(
                getResources().getColor(R.color.purple_700));
    }
    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this, R.style.Theme_CostumesApp_Dialog)
                .setTitle("Quitter")
                .setMessage("Voulez-vous vraiment quitter l'application ?")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Fermer l'application
                        finishAffinity();
                    }
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void performLogout() {
        // 1. Déconnecter de SharedPreferences
        prefsManager.logout();

        // 2. Afficher message
        Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();

        // 3. Rediriger vers LoginActivity
        Intent intent = new Intent(CostumesListActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("CostumesListActivity", "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("CostumesListActivity", "onPause");
    }
}