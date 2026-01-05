package com.example.costumes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.costumes.models.Client;
import com.example.costumes.services.ApiService;
import com.example.costumes.utils.SharedPreferencesManager;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerLink;
    private ProgressBar progressBar;

    private ApiService apiService;
    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d("LoginActivity", "=== ONCREATE LOGIN ACTIVITY ===");

        // Initialisation
        apiService = new ApiService();
        prefsManager = new SharedPreferencesManager(this);

        // Afficher les infos de débogage
        prefsManager.printClientInfo();

        // Vérifier si déjà connecté
        if (prefsManager.isLoggedIn()) {
            Log.d("LoginActivity", "✅ Déjà connecté, redirection...");
            Toast.makeText(this, "Bienvenue " + prefsManager.getUserName(), Toast.LENGTH_SHORT).show();
            goToCostumesList();
            return;
        }

        // Initialiser les vues
        initViews();

        Log.d("LoginActivity", "LoginActivity prête");
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);
        progressBar = findViewById(R.id.progressBar);

        // Remplir email si mémorisé
        String rememberedEmail = prefsManager.getRememberedEmail();
        if (!rememberedEmail.isEmpty()) {
            emailEditText.setText(rememberedEmail);
        }

        // Écouteurs
        loginButton.setOnClickListener(v -> {
            Log.d("LoginActivity", "=== BOUTON LOGIN CLIQUÉ ===");
            handleLogin();
        });

        registerLink.setOnClickListener(v -> {
            Log.d("LoginActivity", "Redirection vers inscription");
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void handleLogin() {
        Log.d("LoginActivity", "=== TRAITEMENT LOGIN ===");

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        Log.d("LoginActivity", "Email: " + email);
        Log.d("LoginActivity", "Password: " + (password.isEmpty() ? "VIDE" : "***"));

        // Validation
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email requis");
            emailEditText.requestFocus();
            Log.e("LoginActivity", "❌ Email vide");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email invalide");
            emailEditText.requestFocus();
            Log.e("LoginActivity", "❌ Email invalide");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Mot de passe requis");
            passwordEditText.requestFocus();
            Log.e("LoginActivity", "❌ Mot de passe vide");
            return;
        }

        // Sauvegarder l'email pour "se souvenir de moi"
        prefsManager.saveRememberedEmail(email);

        // Afficher progression
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);
        loginButton.setText("Connexion...");

        Log.d("LoginActivity", "Appel API login...");

        // Appel API
        apiService.login(email, password, new ApiService.LoginCallback() {
            @Override
            public void onSuccess(Client client, String message) {
                Log.d("LoginActivity", "✅ CALLBACK SUCCESS REÇU!");
                Log.d("LoginActivity", "Client: " + client.getEmail() + ", ID: " + client.getId());

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    loginButton.setText("Se connecter");

                    // Vérifier client
                    if (client == null) {
                        Log.e("LoginActivity", "❌ Client null dans onSuccess!");
                        Toast.makeText(LoginActivity.this,
                                "Erreur: données client manquantes", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Sauvegarder client
                    Log.d("LoginActivity", "Sauvegarde du client...");
                    prefsManager.saveClient(client);

                    // Vérifier sauvegarde
                    Client savedClient = prefsManager.getClient();
                    if (savedClient != null) {
                        Log.d("LoginActivity", "✅ Client sauvegardé: " + savedClient.getEmail());
                        Toast.makeText(LoginActivity.this,
                                "Bonjour " + savedClient.getNom() + "!", Toast.LENGTH_SHORT).show();

                        // Redirection après court délai
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            goToCostumesList();
                        }, 500);
                    } else {
                        Log.e("LoginActivity", "❌ Échec sauvegarde client");
                        Toast.makeText(LoginActivity.this,
                                "Erreur de sauvegarde", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e("LoginActivity", "❌ CALLBACK ERROR: " + error);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    loginButton.setText("Se connecter");

                    Toast.makeText(LoginActivity.this,
                            "Échec connexion: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void goToCostumesList() {
        Log.d("LoginActivity", "=== REDIRECTION VERS COSTUMES LIST ===");

        try {
            Intent intent = new Intent(LoginActivity.this, CostumesListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            Log.d("LoginActivity", "Démarrage de CostumesListActivity...");
            startActivity(intent);

            Log.d("LoginActivity", "Fermeture LoginActivity...");
            finish();

        } catch (Exception e) {
            Log.e("LoginActivity", "❌ Erreur redirection: " + e.getMessage());
            e.printStackTrace();

            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LoginActivity", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("LoginActivity", "onPause");
    }
}