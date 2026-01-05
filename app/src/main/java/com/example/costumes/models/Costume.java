package com.example.costumes.models;

import com.google.gson.annotations.SerializedName;

public class Costume {
    private int id;
    private String nom;
    private String type;
    private String taille;
    private double prix;

    @SerializedName("image_path")
    private String imagePath;

    private boolean disponibilite;

    public Costume() {
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTaille() { return taille; }
    public void setTaille(String taille) { this.taille = taille; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public String getImage_path() { return imagePath; }
    public void setImage_path(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isDisponibilite() { return disponibilite; }
    public void setDisponibilite(boolean disponibilite) {
        this.disponibilite = disponibilite;
    }

    // Méthode utilitaire pour le debug
    public String toString() {
        return "Costume{nom='" + nom + "', prix=" + prix +
                ", disponible=" + disponibilite +
                ", image='" + (imagePath != null ? imagePath.substring(0, Math.min(30, imagePath.length())) : "null") + "'}";
    }
}