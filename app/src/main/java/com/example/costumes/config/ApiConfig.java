package com.example.costumes.config;




/**
 * Configuration de l'API
 *
 * IMPORTANT: Modifiez BASE_URL selon votre environnement:
 *
 * - Émulateur Android: "http://10.0.2.2:8000/api"
 * - Émulateur iOS: "http://localhost:8000/api"
 * - Appareil physique: "http://VOTRE_IP_LOCALE:8000/api"
 *   (Exemple: "http://192.168.1.100:8000/api")
 *
 * Pour trouver votre IP locale:
 * - Windows: ipconfig (cherchez IPv4)
 * - Mac/Linux: ifconfig ou ip addr
 */
public class ApiConfig {
    public static final String BASE_URL = "http://10.0.2.2:8000/api";

    public static final int TIMEOUT_SECONDS = 30;

}
