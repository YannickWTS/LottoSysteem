package be.wts.lottosysteem_Ali.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BestellingView(
        long id,
        long klantId,
        String spelType,
        String maand,
        LocalDate datumRegistratie,
        boolean betaald,
        long medewerkerId,
        LocalDateTime laatsteUpdate
) {}
