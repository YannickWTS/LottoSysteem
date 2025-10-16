package be.wts.lottosysteem_Ali.service;

import be.wts.lottosysteem_Ali.exception.KlantNietGevondenException;
import be.wts.lottosysteem_Ali.model.Klant;
import be.wts.lottosysteem_Ali.repository.BestellingRepository;
import be.wts.lottosysteem_Ali.repository.KlantRepository;
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

    /* ---------- writes ---------- */

    @Transactional
    public long save(Klant klant) {
        var naam  = normalizeNaam(klant.getNaam());
        var email = normalizeEmail(klant.getEmail());

        if (naam == null || naam.isBlank() || email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Naam en e-mail zijn verplicht");
        }

        // nette 409 i.p.v. DB-fout
        if (klantRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail bestaat al");
        }

        klant.setNaam(naam);
        klant.setEmail(email); // lowercase
        return klantRepository.save(klant);
    }

    @Transactional
    public void update(long id, Klant payload) {
        var naam  = normalizeNaam(payload.getNaam());
        var email = normalizeEmail(payload.getEmail());

        if (naam == null || naam.isBlank() || email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Naam en e-mail zijn verplicht");
        }

        // conflict alleen als de e-mail bij een ANDERE klant hoort
        if (klantRepository.existsByEmailIgnoreCaseExcludingId(email, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail bestaat al");
        }

        var updated = klantRepository.update(id, naam, email); // email lowercase
        if (updated == 0) throw new KlantNietGevondenException(id);
    }

    @Transactional
    public void delete(long id) {
        if (bestellingRepository.countByKlantId(id) > 0) {
            throw new IllegalStateException("Klant heeft nog bestellingen en kan niet verwijderd worden.");
        }
        var rows = klantRepository.delete(id);
        if (rows == 0) throw new KlantNietGevondenException(id);
    }
}
