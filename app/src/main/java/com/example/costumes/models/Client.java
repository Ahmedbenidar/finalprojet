package com.example.costumes.models;

public class Client {
    private int id;
    private String nom;
    private String email;
    private String telephone;
    private String adresse;

    public Client() {
    }

    public Client(int id, String nom, String email, String telephone, String adresse) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
}
