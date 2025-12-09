package be.tackit.lottosysteem.model;

public class Gebruiker {
    private final long id;
    private final String gebruikersnaam;
    private String wachtwoord;
    private final String rol;

    public Gebruiker(long id, String gebruikersnaam, String wachtwoord, String rol) {
        this.id = id;
        this.gebruikersnaam = gebruikersnaam;
        this.wachtwoord = wachtwoord;
        this.rol = rol;
    }

    public long getId() {
        return id;
    }

    public String getRol() {
        return rol;
    }

    public String getWachtwoord() {
        return wachtwoord;
    }

    public String getGebruikersnaam() {
        return gebruikersnaam;
    }

    public void setWachtwoord(String wachtwoord) {
        this.wachtwoord = wachtwoord;
    }
}
