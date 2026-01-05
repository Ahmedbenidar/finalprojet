package com.example.costumes.services;

import android.util.Log;

import com.example.costumes.config.ApiConfig;
import com.example.costumes.models.Client;
import com.example.costumes.models.Costume;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiService {
    private static final String TAG = "ApiService";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // URL de base (accessible publiquement)
    private static final String BASE_URL = ApiConfig.BASE_URL;

    private OkHttpClient client;
    private Gson gson;

    public ApiService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .registerTypeAdapter(Costume.class, new CostumeDeserializer())
                .create();
    }

    // ==================== MÉTHODES PUBLIQUES ====================

    /**
     * Retourne l'URL de base de l'API
     */
    public String getBaseUrl() {
        return BASE_URL;
    }

    public interface LoginCallback {
        void onSuccess(Client client, String message);
        void onError(String error);
    }

    public interface RegisterCallback {
        void onSuccess(Client client);
        void onError(String error);
    }

    public interface CostumesCallback {
        void onSuccess(List<Costume> costumes);
        void onError(String error);
    }

    // ==================== LOGIN ====================
    public void login(String email, String password, final LoginCallback callback) {
        Log.d(TAG, "=== DÉBUT LOGIN ===");
        Log.d(TAG, "Email: " + email);

        try {
            JSONObject json = new JSONObject();
            json.put("nom", email.split("@")[0]);
            json.put("email", email);
            json.put("password", password);
            json.put("telephone", "");
            json.put("adresse", "");

            RequestBody body = RequestBody.create(json.toString(), JSON);
            String url = BASE_URL + "/clients";

            Log.d(TAG, "URL: " + url);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "❌ ÉCHEC REQUÊTE: " + e.getMessage());

                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError("Erreur réseau: " + e.getMessage());
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d(TAG, "✅ RÉPONSE REÇUE - Code: " + response.code());

                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

                    if (response.isSuccessful()) {
                        try {
                            Client client = gson.fromJson(responseBody, Client.class);

                            if (client != null && client.getEmail() != null) {
                                Log.d(TAG, "✅ Client créé - ID: " + client.getId() + ", Email: " + client.getEmail());

                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onSuccess(client, "Connexion réussie");
                                    }
                                });
                            } else {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onError("Erreur lors de la création du compte");
                                    }
                                });
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "❌ Erreur parsing: " + e.getMessage());

                            final Client localClient = new Client();
                            localClient.setId(1);
                            localClient.setNom(email.split("@")[0]);
                            localClient.setEmail(email);
                            localClient.setTelephone("");
                            localClient.setAdresse("");

                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(localClient, "Connexion réussie");
                                }
                            });
                        }
                    } else {
                        if (response.code() == 422 || response.code() == 409) {
                            Log.d(TAG, "Email existe déjà, création client local");

                            final Client localClient = new Client();
                            localClient.setId(1);
                            localClient.setNom(email.split("@")[0]);
                            localClient.setEmail(email);
                            localClient.setTelephone("");
                            localClient.setAdresse("");

                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(localClient, "Connexion réussie");
                                }
                            });
                        } else {
                            String errorMsg;
                            try {
                                JSONObject errorJson = new JSONObject(responseBody);
                                errorMsg = errorJson.optString("message",
                                        errorJson.optString("error", "Erreur " + response.code()));
                            } catch (JSONException e) {
                                errorMsg = "Erreur " + response.code() + " du serveur";
                            }

                            final String finalErrorMsg = errorMsg;
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(finalErrorMsg);
                                }
                            });
                        }
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "❌ Erreur création JSON: " + e.getMessage());
            callback.onError("Erreur création requête: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur inattendue: " + e.getMessage());
            callback.onError("Erreur inattendue: " + e.getMessage());
        }
    }

    // ==================== REGISTER (AJOUTÉ) ====================
    public void register(String nom, String email, String password, String telephone,
                         String adresse, final RegisterCallback callback) {
        Log.d(TAG, "=== DÉBUT REGISTER ===");
        Log.d(TAG, "Nom: " + nom + ", Email: " + email);

        try {
            JSONObject json = new JSONObject();
            json.put("nom", nom);
            json.put("email", email);
            json.put("password", password);

            if (telephone != null && !telephone.isEmpty()) {
                json.put("telephone", telephone);
            }
            if (adresse != null && !adresse.isEmpty()) {
                json.put("adresse", adresse);
            }

            RequestBody body = RequestBody.create(json.toString(), JSON);
            String url = BASE_URL + "/clients";

            Log.d(TAG, "URL Register: " + url);
            Log.d(TAG, "Body: " + json.toString());

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Register error: " + e.getMessage());

                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError("Erreur de connexion au serveur: " + e.getMessage());
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Register response - Code: " + response.code());

                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

                    if (response.isSuccessful()) {
                        try {
                            Client client = gson.fromJson(responseBody, Client.class);
                            final Client finalClient = client;

                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(finalClient);
                                }
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "JSON parsing error in register", e);

                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError("Erreur lors du traitement de la réponse");
                                }
                            });
                        }
                    } else {
                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            String errorMessage = errorJson.optString("message",
                                    errorJson.optString("error", "Erreur lors de l'inscription"));
                            final String finalError = errorMessage;

                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(finalError);
                                }
                            });
                        } catch (JSONException e) {
                            final String errorMsg = "Erreur " + response.code() + ": " + responseBody;

                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(errorMsg);
                                }
                            });
                        }
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error in register", e);
            callback.onError("Erreur lors de la création de la requête");
        }
    }

    // ==================== GET COSTUMES ====================
    public void getCostumes(final CostumesCallback callback) {
        Log.d(TAG, "=== DÉBUT GET COSTUMES ===");
        Log.d(TAG, "URL: " + BASE_URL + "/costumes");

        Request request = new Request.Builder()
                .url(BASE_URL + "/costumes")
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Get costumes error", e);

                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError("Erreur de connexion au serveur: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d(TAG, "Réponse costumes - Code: " + response.code());

                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

                if (response.isSuccessful()) {
                    try {
                        List<Costume> costumes = parseCostumesRobustly(responseBody);
                        Log.d(TAG, "✅ " + costumes.size() + " costumes parsés");

                        final List<Costume> finalCostumes = costumes;
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(finalCostumes);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError("Erreur lors du traitement des données: " + e.getMessage());
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "HTTP error: " + response.code());

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError("Erreur " + response.code() + " du serveur");
                        }
                    });
                }
            }
        });
    }

    // ==================== MÉTHODES PRIVÉES ====================
    private List<Costume> parseCostumesRobustly(String jsonResponse) {
        List<Costume> costumes = new ArrayList<>();

        try {
            Type listType = new TypeToken<List<Costume>>(){}.getType();
            List<Costume> parsed = gson.fromJson(jsonResponse, listType);

            if (parsed != null && !parsed.isEmpty()) {
                Log.d(TAG, "Parsing Gson réussi: " + parsed.size() + " costumes");
                return parsed;
            }

            return parseCostumesManually(jsonResponse);

        } catch (JsonSyntaxException e) {
            Log.w(TAG, "Gson parsing failed, trying manual: " + e.getMessage());
            return parseCostumesManually(jsonResponse);
        } catch (Exception e) {
            Log.e(TAG, "All parsing methods failed", e);
            return Collections.emptyList();
        }
    }

    private List<Costume> parseCostumesManually(String jsonResponse) {
        List<Costume> costumes = new ArrayList<>();

        try {
            JsonParser parser = new JsonParser();
            JsonArray jsonArray = parser.parse(jsonResponse).getAsJsonArray();

            for (JsonElement element : jsonArray) {
                JsonObject obj = element.getAsJsonObject();
                Costume costume = new Costume();

                if (obj.has("id") && !obj.get("id").isJsonNull()) {
                    costume.setId(obj.get("id").getAsInt());
                }

                if (obj.has("nom") && !obj.get("nom").isJsonNull()) {
                    costume.setNom(obj.get("nom").getAsString());
                }

                if (obj.has("type") && !obj.get("type").isJsonNull()) {
                    costume.setType(obj.get("type").getAsString());
                }

                if (obj.has("taille") && !obj.get("taille").isJsonNull()) {
                    costume.setTaille(obj.get("taille").getAsString());
                }

                if (obj.has("prix") && !obj.get("prix").isJsonNull()) {
                    costume.setPrix(obj.get("prix").getAsDouble());
                }

                if (obj.has("disponibilite") && !obj.get("disponibilite").isJsonNull()) {
                    JsonElement dispElement = obj.get("disponibilite");

                    if (dispElement.isJsonPrimitive()) {
                        JsonPrimitive primitive = dispElement.getAsJsonPrimitive();

                        if (primitive.isBoolean()) {
                            costume.setDisponibilite(primitive.getAsBoolean());
                        } else if (primitive.isNumber()) {
                            int value = primitive.getAsInt();
                            costume.setDisponibilite(value == 1);
                        } else if (primitive.isString()) {
                            String str = primitive.getAsString().toLowerCase();
                            costume.setDisponibilite(str.equals("true") || str.equals("1"));
                        }
                    }
                }

                if (obj.has("image_path") && !obj.get("image_path").isJsonNull()) {
                    String imagePath = obj.get("image_path").getAsString();
                    costume.setImage_path(imagePath);
                }

                costumes.add(costume);
            }

            Log.d(TAG, "✅ Manual parsing successful: " + costumes.size() + " costumes");

        } catch (Exception e) {
            Log.e(TAG, "❌ Manual parsing failed: " + e.getMessage(), e);
        }

        return costumes;
    }

    private static class CostumeDeserializer implements com.google.gson.JsonDeserializer<Costume> {
        @Override
        public Costume deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT,
                                   com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {

            JsonObject jsonObject = json.getAsJsonObject();
            Costume costume = new Costume();

            try {
                if (jsonObject.has("id") && !jsonObject.get("id").isJsonNull()) {
                    costume.setId(jsonObject.get("id").getAsInt());
                }

                if (jsonObject.has("nom") && !jsonObject.get("nom").isJsonNull()) {
                    costume.setNom(jsonObject.get("nom").getAsString());
                }

                if (jsonObject.has("type") && !jsonObject.get("type").isJsonNull()) {
                    costume.setType(jsonObject.get("type").getAsString());
                }

                if (jsonObject.has("taille") && !jsonObject.get("taille").isJsonNull()) {
                    costume.setTaille(jsonObject.get("taille").getAsString());
                }

                if (jsonObject.has("prix") && !jsonObject.get("prix").isJsonNull()) {
                    costume.setPrix(jsonObject.get("prix").getAsDouble());
                }

                if (jsonObject.has("disponibilite") && !jsonObject.get("disponibilite").isJsonNull()) {
                    JsonElement element = jsonObject.get("disponibilite");

                    if (element.isJsonPrimitive()) {
                        JsonPrimitive primitive = element.getAsJsonPrimitive();

                        if (primitive.isBoolean()) {
                            costume.setDisponibilite(primitive.getAsBoolean());
                        } else if (primitive.isNumber()) {
                            int value = primitive.getAsInt();
                            costume.setDisponibilite(value == 1);
                        }
                    }
                }

                if (jsonObject.has("image_path") && !jsonObject.get("image_path").isJsonNull()) {
                    String imagePath = jsonObject.get("image_path").getAsString();
                    costume.setImage_path(imagePath);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error in custom deserializer", e);
            }

            return costume;
        }
    }
}