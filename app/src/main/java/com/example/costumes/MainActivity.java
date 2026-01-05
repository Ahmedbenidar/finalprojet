package com.example.costumes;



import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.costumes.utils.SharedPreferencesManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferencesManager prefsManager = new SharedPreferencesManager(this);

        // Délai pour l'écran de démarrage
        new Handler().postDelayed(() -> {
            if (prefsManager.isLoggedIn()) {
                // L'utilisateur est déjà connecté, aller à la liste des costumes
                startActivity(new Intent(MainActivity.this, CostumesListActivity.class));
            } else {
                // L'utilisateur n'est pas connecté, aller à la page de connexion
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
            finish();
        }, 1000);
    }
}
