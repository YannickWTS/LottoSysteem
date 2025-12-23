package be.tackit.lottosysteem.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class Klant {
    private long id;
    @NotBlank(message = "Naam is verplicht!")
    private String naam;
    @NotBlank(message = "Email is verplicht!")
    @Email(message = "ongeldig e-mailadres.")
    private String email;
    private boolean verwijderd;
    private LocalDateTime verwijderdOp;

    public Klant() {}

    public Klant(long id) {
        this.id = id;
    }

    public Klant(long id, String naam, String email) {
        this.id = id;
        this.naam = naam;
        this.email = email;
    }

    public Klant(long id, String naam, String email, boolean verwijderd, LocalDateTime verwijderdOp) {
        this.id = id;
        this.naam = naam;
        this.email = email;
        this.verwijderd = verwijderd;
        this.verwijderdOp = verwijderdOp;
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

    public boolean isVerwijderd() {
        return verwijderd;
    }

    public LocalDateTime getVerwijderdOp() {
        return verwijderdOp;
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

    public void setVerwijderd(boolean verwijderd) {
        this.verwijderd = verwijderd;
    }

    public void setVerwijderdOp(LocalDateTime verwijderdOp) {
        this.verwijderdOp = verwijderdOp;
    }
}
