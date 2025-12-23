package be.tackit.lottosysteem.service;

import be.tackit.lottosysteem.exception.KlantNietGevondenException;
import be.tackit.lottosysteem.model.Klant;
import be.tackit.lottosysteem.repository.BestellingRepository;
import be.tackit.lottosysteem.repository.KlantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class KlantService {
    private final KlantRepository klantRepository;
    private final BestellingRepository bestellingRepository;

    public KlantService(KlantRepository klantRepository, BestellingRepository bestellingRepository) {
        this.klantRepository = klantRepository;
        this.bestellingRepository = bestellingRepository;
    }

    /* ---------- helpers ---------- */

    private static String normalizeNaam(String naam) {
        return naam == null ? null : naam.trim();
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    /* ---------- reads ---------- */

    public List<Klant> findAll() {
        return klantRepository.findAll();
    }

    public Klant findById(long id) {
        return klantRepository.findById(id)
                .orElseThrow(() -> new KlantNietGevondenException(id));
    }

    public List<Klant> findByNaamBevat(String naamDeel) {
        return klantRepository.findByNaamBevat((naamDeel == null) ? "" : naamDeel.trim());
    }

    /**
     * Voor autocomplete / klant selecteren: enkel actieve klanten.
     */
    public List<Klant> findActiveByNaamBevat(String naamDeel) {
        return klantRepository.findActiveByNaamBevat((naamDeel == null) ? "" : naamDeel.trim());
    }

    /* ---------- writes ---------- */

    @Transactional
    public long save(Klant klant) {
        var naam  = normalizeNaam(klant.getNaam());
        var email = normalizeEmail(klant.getEmail());

        if (naam == null || naam.isBlank() || email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Naam en e-mail zijn verplicht");
        }

        if (klantRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail bestaat al");
        }

        klant.setNaam(naam);
        klant.setEmail(email);
        return klantRepository.save(klant);
    }

    @Transactional
    public void update(long id, Klant payload) {
        var naam  = normalizeNaam(payload.getNaam());
        var email = normalizeEmail(payload.getEmail());

        if (naam == null || naam.isBlank() || email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Naam en e-mail zijn verplicht");
        }

        if (klantRepository.existsByEmailIgnoreCaseExcludingId(email, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail bestaat al");
        }

        var updated = klantRepository.update(id, naam, email);
        if (updated == 0) throw new KlantNietGevondenException(id);
    }

    /**
     * "Verwijderen" = anonimiseren.
     * Enkel toegestaan als er GEEN openstaande (onbetaalde) bestellingen zijn.
     */
    @Transactional
    public void anonymize(long id) {
        // Klant moet bestaan (zodat we 404 kunnen geven i.p.v. "ok maar 0 rows")
        findById(id);

        // BELANGRIJK: check enkel openstaande, niet "heeft ooit besteld"
        // → hiervoor moet BestellingRepository een count voor onbetaald hebben.
        if (bestellingRepository.countOpenstaandByKlantId(id) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Klant heeft nog openstaande (onbetaalde) bestellingen en kan niet verwijderd worden."
            );
        }

        var rows = klantRepository.anonymize(id);
        if (rows == 0) throw new KlantNietGevondenException(id);
    }
}
