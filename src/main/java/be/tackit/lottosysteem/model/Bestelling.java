package be.tackit.lottosysteem.model;

import java.time.LocalDateTime;

public class Bestelling {
    private final long id;
    private final Klant klant;
    private final String spelType;
    private final String maand;
    private final LocalDateTime datumRegistratie;
    private final boolean betaald;
    private final long medewerkerId;
    private final String medewerkerNaam;
    private final LocalDateTime laatsteUpdate;

    public Bestelling(long id, Klant klant, String spelType, String maand, LocalDateTime datumRegistratie,
            boolean betaald, long medewerkerId, String medewerkerNaam, LocalDateTime laatsteUpdate) {
        this.id = id;
        this.klant = klant;
        this.spelType = spelType;
        this.maand = maand;
        this.datumRegistratie = datumRegistratie;
        this.betaald = betaald;
        this.medewerkerId = medewerkerId;
        this.medewerkerNaam = medewerkerNaam;
        this.laatsteUpdate = laatsteUpdate;
    }

    public Bestelling(Klant klant, String spelType, String maand, LocalDateTime datumRegistratie, boolean betaald,
            long medewerkerId, String medewerkerNaam) {
        this(0L, klant, spelType, maand, datumRegistratie, betaald, medewerkerId, medewerkerNaam, null);
    }

    public long getId() {
        return id;
    }

    public Klant getKlant() {
        return klant;
    }

    public String getSpelType() {
        return spelType;
    }

    public String getMaand() {
        return maand;
    }

    public LocalDateTime getDatumRegistratie() {
        return datumRegistratie;
    }

    public boolean isBetaald() {
        return betaald;
    }

    public long getMedewerkerId() {
        return medewerkerId;
    }

    public String getMedewerkerNaam() {
        return medewerkerNaam;
    }

    public LocalDateTime getLaatsteUpdate() {
        return laatsteUpdate;
    }
}
