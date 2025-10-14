package be.wts.lottosysteem_Ali.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Bestelling {
    private final long id;
    private final Klant klant;
    private final String spelType;
    private final String maand;
    private final LocalDate datumRegistratie;
    private final boolean betaald;
    private final long medewerkerId;
    private final LocalDateTime laatsteUpdate;

    public Bestelling(long id, Klant klant, String spelType, String maand, LocalDate datumRegistratie, boolean betaald, long medewerkerId, LocalDateTime laatsteUpdate) {
        this.id = id;
        this.klant = klant;
        this.spelType = spelType;
        this.maand = maand;
        this.datumRegistratie = datumRegistratie;
        this.betaald = betaald;
        this.medewerkerId = medewerkerId;
        this.laatsteUpdate = laatsteUpdate;
    }

    public Bestelling(Klant klant, String spelType, String maand, LocalDate datumRegistratie, boolean betaald, long medewerkerId) {
        this(0L, klant, spelType, maand, datumRegistratie, betaald, medewerkerId, null);
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

    public LocalDate getDatumRegistratie() {
        return datumRegistratie;
    }

    public boolean isBetaald() {
        return betaald;
    }

    public long getMedewerkerId() {
        return medewerkerId;
    }

    public LocalDateTime getLaatsteUpdate() {
        return laatsteUpdate;
    }
}
