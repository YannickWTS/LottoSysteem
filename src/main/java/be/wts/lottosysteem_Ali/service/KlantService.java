package be.wts.lottosysteem_Ali.service;

import be.wts.lottosysteem_Ali.exception.KlantNietGevondenException;
import be.wts.lottosysteem_Ali.model.Klant;
import be.wts.lottosysteem_Ali.repository.BestellingRepository;
import be.wts.lottosysteem_Ali.repository.KlantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class KlantService {
    private final KlantRepository klantRepository;
    private final BestellingRepository bestellingRepository;

    public KlantService(KlantRepository klantRepository, BestellingRepository bestellingRepository) {
        this.klantRepository = klantRepository;
        this.bestellingRepository = bestellingRepository;
    }

    public List<Klant> findAll() {
        return klantRepository.findAll();
    }

    public Klant findById(long id) {
        return klantRepository.findById(id)
                .orElseThrow(() -> new KlantNietGevondenException(id));
    }

    public List<Klant> findByNaamBevat(String naamDeel) {
        return klantRepository.findByNaamBevat(naamDeel.trim());
    }

    @Transactional
    public long save(Klant klant) {
        var naam = klant.getNaam().trim();
        var email = klant.getEmail().trim();
        klant.setNaam(naam);
        klant.setEmail(email);
        return klantRepository.save(klant);
    }

    @Transactional
    public void update(long id, Klant payload) {
        var updated = klantRepository.update(
                id,
                payload.getNaam().trim(),
                payload.getEmail().trim()
        );
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
