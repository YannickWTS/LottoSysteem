package be.wts.lottosysteem_Ali.model;

import jakarta.validation.constraints.Email;

public class Klant {
    private long id;
    private String naam;
    private String email;

    public Klant() {}

    public Klant(long id) {
        this.id = id;
    }

    public Klant(long id, String naam, String email) {
        this.id = id;
        this.naam = naam;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getNaam() {
        return naam;
    }

    public String getEmail() {
        return email;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
