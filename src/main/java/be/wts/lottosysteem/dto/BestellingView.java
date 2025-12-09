package be.wts.lottosysteem.dto;

import java.time.LocalDateTime;

public record BestellingView(
        long id,
        long klantId,
        String klantNaam,
        String spelType,
        String maand,
        LocalDateTime datumRegistratie,
        boolean betaald,
        long medewerkerId,
        String medewerkerNaam,
        LocalDateTime laatsteUpdate
) {}
