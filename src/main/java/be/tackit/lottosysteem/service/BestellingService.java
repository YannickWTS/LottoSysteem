package be.tackit.lottosysteem.service;

import be.tackit.lottosysteem.dto.NieuweBestelling;
import be.tackit.lottosysteem.model.Bestelling;
import be.tackit.lottosysteem.model.Klant;
import be.tackit.lottosysteem.repository.BestellingRepository;
import be.tackit.lottosysteem.repository.GebruikerRepository;
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
    private final PrintService printService;

    public BestellingService(BestellingRepository bestellingRepository, GebruikerRepository gebruikerRepository,
            PrintService printService) {
        this.bestellingRepository = bestellingRepository;
        this.gebruikerRepository = gebruikerRepository;
        this.printService = printService;
    }

    private static final List<String> TOEGESTANE_SPELTYPES = List.of(
            "Lotto",
            "Lotto Extra",
            "EuroMillions",
            "EuroMillions Extra");

    public List<Bestelling> getAllBestelling() {
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
                gebruiker.getId(),
                gebruiker.getGebruikersnaam());

        long newId = bestellingRepository.save(bestelling);

        if (betaald) {
            try {
                // We moeten de volledige bestelling ophalen om de klantnaam e.d. te hebben voor
                // de bon
                Bestelling savedBestelling = bestellingRepository.findById(newId);
                printService.printBestelling(savedBestelling);
            } catch (Exception e) {
                System.err.println("Kon bon niet afdrukken bij aanmaken: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return newId;
    }

    public void setBetaald(long id, boolean betaald) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        // A) via Optional<Long>:
        var bewerkerId = gebruikerRepository.findIdByGebruikersnaam(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Ingelogde gebruiker niet gevonden"));

        // B) of via Optional<Gebruiker> (dan .map(Gebruiker::getId))

        int updated = bestellingRepository.updateBetaald(id, betaald, bewerkerId);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bestelling niet gevonden");
        }

        // Als er betaald is (= true), dan printen
        if (betaald) {
            try {
                // Haal de volledige bestelling op (inclusief klantgegevens etc.)
                Bestelling b = bestellingRepository.findById(id);
                printService.printBestelling(b);
            } catch (Exception e) {
                // Log de fout, maar laat de request niet falen (transactioneel gezien apart, of
                // gewoon 'best effort')
                System.err.println("Kon bon niet afdrukken: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void deleteBestelling(long id) {
        var bestelling = bestellingRepository.findById(id);

        if (bestelling.isBetaald()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Betaalde bestelling kan niet verwijderd worden");
        }

        bestellingRepository.delete(id);
    }

    public List<Bestelling> getAllBestellingenVoorKlant(long klantId) {
        if (klantId <= 0)
            throw new IllegalArgumentException("Ongeldige klantId!");
        return bestellingRepository.findAllByKlantId(klantId);
    }
}
