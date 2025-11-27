package be.wts.lottosysteem_Ali.service;

import be.wts.lottosysteem_Ali.dto.NieuweBestelling;
import be.wts.lottosysteem_Ali.model.Bestelling;
import be.wts.lottosysteem_Ali.model.Klant;
import be.wts.lottosysteem_Ali.repository.BestellingRepository;
import be.wts.lottosysteem_Ali.repository.GebruikerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BestellingService {
    private final BestellingRepository bestellingRepository;
    private final GebruikerRepository gebruikerRepository;

    public BestellingService(BestellingRepository bestellingRepository, GebruikerRepository gebruikerRepository) {
        this.bestellingRepository = bestellingRepository;
        this.gebruikerRepository = gebruikerRepository;
    }

    private static final List<String> TOEGESTANE_SPELTYPES = List.of(
            "Lotto",
            "Lotto Extra",
            "EuroMillions",
            "EuroMillions Extra"
    );

    public List<Bestelling> getAllBestelling(){
        return bestellingRepository.findAll();
    }

    public long addBestelling(NieuweBestelling dto) {
        if (dto.spelType() == null || dto.spelType().isBlank())
            throw new IllegalArgumentException("Speltype mag niet leeg zijn!");
        if (!TOEGESTANE_SPELTYPES.contains(dto.spelType()))
            throw new IllegalArgumentException("Ongeldig speltype!");
        if (dto.klantId() <= 0)
            throw new IllegalArgumentException("Klant verplicht!");
        var betaald = dto.betaald() != null && dto.betaald();

        // maand basic check (optioneel strikter maken)
        if (dto.maand() == null || dto.maand().isBlank())
            throw new IllegalArgumentException("Maand verplicht!");

        // Medewerker = ingelogde user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var gebruiker = gebruikerRepository.findByGebruikersnaam(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Ingelogde gebruiker niet gevonden"));

        var bestelling = new Bestelling(
                new Klant(dto.klantId()),
                dto.spelType(),
                dto.maand(),
                LocalDateTime.now(),
                betaald,
                gebruiker.getId()
        );

        return bestellingRepository.save(bestelling);
    }

    public void setBetaald(long id, boolean betaald) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        // Kies één van beide (wat jij al hebt):
        // A) via Optional<Long>:
        var bewerkerId = gebruikerRepository.findIdByGebruikersnaam(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Ingelogde gebruiker niet gevonden"));

        // B) of via Optional<Gebruiker> (dan .map(Gebruiker::getId))

        int updated = bestellingRepository.updateBetaald(id, betaald, bewerkerId);
        if (updated == 0) {
            // niets gevonden → 404 is logischer dan 409
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bestelling niet gevonden");
        }
    }


    public void deleteBestelling(long id) {
        bestellingRepository.delete(id);
    }
}
