package be.wts.lottosysteem_Ali.service;

import be.wts.lottosysteem_Ali.exception.KlantNietGevondenException;
import be.wts.lottosysteem_Ali.model.Klant;
import be.wts.lottosysteem_Ali.repository.KlantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class KlantService {
    private final KlantRepository klantRepository;

    public KlantService(KlantRepository klantRepository) {
        this.klantRepository = klantRepository;
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
        var deleted = klantRepository.delete(id);
        if (deleted == 0) throw new KlantNietGevondenException(id);
    }
}
