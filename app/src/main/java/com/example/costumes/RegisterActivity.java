package com.example.costumes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.costumes.services.ApiService;
import com.example.costumes.utils.SharedPreferencesManager;

public class RegisterActivity extends AppCompatActivity {

    private EditText nomEditText;
    private EditText emailEditText;
    private EditText telephoneEditText;
    private EditText adresseEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button registerButton;
    private TextView loginLink;
    private ProgressBar progressBar;

    private ApiService apiService;
    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiService = new ApiService();
        prefsManager = new SharedPreferencesManager(this);

        nomEditText = findViewById(R.id.nomEditText);
        emailEditText = findViewById(R.id.emailEditText);
        telephoneEditText = findViewById(R.id.telephoneEditText);
        adresseEditText = findViewById(R.id.adresseEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);
        progressBar = findViewById(R.id.progressBar);

        registerButton.setOnClickListener(v -> handleRegister());
        loginLink.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String nom = nomEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String telephone = telephoneEditText.getText().toString().trim();
        String adresse = adresseEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        // Validation
        if (TextUtils.isEmpty(nom)) {
            nomEditText.setError(getString(R.string.required_field));
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.required_field));
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.invalid_email));
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.required_field));
            return;
        }

        if (password.length() < 8) {
            passwordEditText.setError(getString(R.string.password_too_short));
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError(getString(R.string.passwords_not_match));
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        String finalTelephone = TextUtils.isEmpty(telephone) ? null : telephone;
        String finalAdresse = TextUtils.isEmpty(adresse) ? null : adresse;

        apiService.register(nom, email, password, finalTelephone, finalAdresse, new ApiService.RegisterCallback() {
            @Override
            public void onSuccess(com.example.costumes.models.Client client) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    registerButton.setEnabled(true);

                    // CORRECTION : Après inscription, retourner à LoginActivity
                    // pour que l'utilisateur puisse se connecter
                    Toast.makeText(RegisterActivity.this,
                            "Inscription réussie ! Connectez-vous maintenant",
                            Toast.LENGTH_LONG).show();

                    // Option 1: Retourner à LoginActivity
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);

                    // Option 2: Pré-remplir l'email dans LoginActivity (optionnel)
                    // intent.putExtra("email", email);

                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    registerButton.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}