package be.wts.lottosysteem_Ali.service;

import be.wts.lottosysteem_Ali.model.Bestelling;
import be.wts.lottosysteem_Ali.repository.BestellingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BestellingService {
    private final BestellingRepository bestellingRepository;
    public BestellingService(BestellingRepository bestellingRepository) {
        this.bestellingRepository = bestellingRepository;
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

    public long addBestelling(Bestelling bestelling) {
        // Validatie: speltype mag niet leeg zijn
        if (bestelling.getSpelType() == null || bestelling.getSpelType().isEmpty()) {
            throw new IllegalArgumentException("Speltype mag niet leeg zijn!");
        }

        // Validatie: speltype moet geldig zijn
        if (!TOEGESTANE_SPELTYPES.contains(bestelling.getSpelType())) {
            throw new IllegalArgumentException("Ongeldig speltype!");
        }

        // Validatie: klant mag niet null zijn.
        if (bestelling.getKlant() == null || bestelling.getKlant().getId() <= 0) {
            throw new IllegalArgumentException("Klant verplicht!");
        }

        // Validatie: datumRegistratie mag niet null zijn
        if (bestelling.getDatumRegistratie() == null) {
            throw new IllegalArgumentException("Datum van registratie mag niet null zijn!");
        }

        return bestellingRepository.save(bestelling);
    }

    public void deleteBestelling(long id) {
        bestellingRepository.delete(id);
    }
}
