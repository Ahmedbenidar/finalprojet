package com.example.costumes.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.costumes.models.Client;
import com.google.gson.Gson;

public class SharedPreferencesManager {
    private static final String PREFS_NAME = "CostumesAppPrefs";
    private static final String KEY_CLIENT = "client";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_REMEMBER_EMAIL = "remember_email";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Gson gson;

    public SharedPreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();
    }

    // ==================== MÉTHODES PRINCIPALES ====================

    /**
     * Sauvegarder le client après connexion
     */
    public void saveClient(Client client) {
        if (client == null) {
            Log.e("SharedPrefs", "Tentative de sauvegarde d'un client null");
            return;
        }

        String clientJson = gson.toJson(client);
        editor.putString(KEY_CLIENT, clientJson);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        boolean success = editor.commit(); // Utilisez commit() pour être sûr que c'est sauvegardé

        if (success) {
            Log.d("SharedPrefs", "Client sauvegardé: " + client.getNom() + " (" + client.getEmail() + ")");
        } else {
            Log.e("SharedPrefs", "Échec de la sauvegarde du client");
        }
    }

    /**
     * Récupérer le client complet
     */
    public Client getClient() {
        String clientJson = prefs.getString(KEY_CLIENT, null);
        if (clientJson != null && !clientJson.isEmpty()) {
            try {
                return gson.fromJson(clientJson, Client.class);
            } catch (Exception e) {
                Log.e("SharedPrefs", "Erreur lors du parsing du client: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * Vérifier si l'utilisateur est connecté
     */
    public boolean isLoggedIn() {
        boolean loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        Log.d("SharedPrefs", "isLoggedIn: " + loggedIn);
        return loggedIn;
    }

    /**
     * Déconnexion
     */
    public void logout() {
        Log.d("SharedPrefs", "Déconnexion de l'utilisateur");
        editor.remove(KEY_CLIENT);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    /**
     * Effacer toutes les données (pour le débogage)
     */
    public void clearAll() {
        Log.d("SharedPrefs", "Effacement de toutes les données");
        editor.clear();
        editor.apply();
    }

    // ==================== GETTERS SIMPLIFIÉS ====================

    /**
     * Récupérer l'ID de l'utilisateur (retourne -1 si non trouvé)
     */
    public int getUserId() {
        Client client = getClient();
        if (client != null) {
            try {
                // Vérifie si getId() existe et retourne un int
                return client.getId();
            } catch (Exception e) {
                Log.e("SharedPrefs", "Erreur getUserId: " + e.getMessage());
                return -1;
            }
        }
        return -1;
    }

    /**
     * Récupérer l'email de l'utilisateur
     */
    public String getUserEmail() {
        Client client = getClient();
        if (client != null && client.getEmail() != null) {
            return client.getEmail();
        }
        return "";
    }

    /**
     * Récupérer le nom de l'utilisateur
     */
    public String getUserName() {
        Client client = getClient();
        if (client != null && client.getNom() != null) {
            return client.getNom();
        }
        return "";
    }

    /**
     * Récupérer le téléphone de l'utilisateur
     */
    public String getUserPhone() {
        Client client = getClient();
        if (client != null && client.getTelephone() != null) {
            return client.getTelephone();
        }
        return "";
    }

    /**
     * Récupérer l'adresse de l'utilisateur
     */
    public String getUserAddress() {
        Client client = getClient();
        if (client != null && client.getAdresse() != null) {
            return client.getAdresse();
        }
        return "";
    }

    // ==================== FONCTION "SE SOUVENIR DE MOI" ====================

    /**
     * Sauvegarder l'email pour "Se souvenir de moi"
     */
    public void saveRememberedEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            editor.putString(KEY_REMEMBER_EMAIL, email.trim());
            editor.apply();
            Log.d("SharedPrefs", "Email mémorisé: " + email);
        }
    }

    /**
     * Récupérer l'email mémorisé
     */
    public String getRememberedEmail() {
        return prefs.getString(KEY_REMEMBER_EMAIL, "");
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Vérifier si un client valide est sauvegardé
     */
    public boolean hasValidClient() {
        Client client = getClient();
        return client != null &&
                client.getEmail() != null &&
                !client.getEmail().isEmpty() &&
                client.getId() > 0; // ou != null si ID est String
    }

    /**
     * Afficher toutes les informations du client (pour débogage)
     */
    public void printClientInfo() {
        Client client = getClient();
        if (client != null) {
            Log.d("SharedPrefs", "=== INFORMATIONS CLIENT ===");
            Log.d("SharedPrefs", "ID: " + client.getId());
            Log.d("SharedPrefs", "Nom: " + client.getNom());
            Log.d("SharedPrefs", "Email: " + client.getEmail());
            Log.d("SharedPrefs", "Téléphone: " + (client.getTelephone() != null ? client.getTelephone() : "Non défini"));
            Log.d("SharedPrefs", "Adresse: " + (client.getAdresse() != null ? client.getAdresse() : "Non définie"));
            Log.d("SharedPrefs", "===========================");
        } else {
            Log.d("SharedPrefs", "Aucun client sauvegardé dans les préférences");
        }

        // Afficher aussi le statut de connexion
        Log.d("SharedPrefs", "Statut connexion: " + isLoggedIn());
        Log.d("SharedPrefs", "Email mémorisé: " + getRememberedEmail());
    }

    /**
     * Vérifier si l'email correspond à l'utilisateur connecté
     */
    public boolean isCurrentUser(String email) {
        if (email == null || email.isEmpty()) return false;
        String currentEmail = getUserEmail();
        return email.equals(currentEmail);
    }

    /**
     * Mettre à jour uniquement certaines informations du client
     */
    public void updateClientInfo(String nom, String telephone, String adresse) {
        Client client = getClient();
        if (client != null) {
            if (nom != null) client.setNom(nom);
            if (telephone != null) client.setTelephone(telephone);
            if (adresse != null) client.setAdresse(adresse);
            saveClient(client);
            Log.d("SharedPrefs", "Informations client mises à jour");
        }
    }
}