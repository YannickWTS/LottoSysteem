package be.wts.lottosysteem_Ali.model;

import java.time.LocalDate;

public class Bestelling {
    private final long id;
    private final long klantId;
    private final String spelType;
    private final String maand;
    private final LocalDate datumRegistratie;
    private final boolean betaald;

    public Bestelling(long id, long klantId, String spelType, String maand, LocalDate datumRegistratie, boolean betaald) {
        this.id = id;
        this.klantId = klantId;
        this.spelType = spelType;
        this.maand = maand;
        this.datumRegistratie = datumRegistratie;
        this.betaald = betaald;
    }

    public long getId() {
        return id;
    }

    public long getKlantId() {
        return klantId;
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
}
