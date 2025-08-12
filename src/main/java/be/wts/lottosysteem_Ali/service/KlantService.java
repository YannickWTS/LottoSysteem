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

    public List<Klant> findByNaamBegintMet(String naamDeel) {
        return klantRepository.findByNaamBegintMet(naamDeel);
    }

    @Transactional
    public long save(Klant klant) {
        return klantRepository.save(klant);
    }
}
