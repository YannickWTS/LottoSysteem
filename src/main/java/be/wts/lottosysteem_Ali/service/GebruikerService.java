package be.wts.lottosysteem_Ali.service;

import be.wts.lottosysteem_Ali.dto.GebruikerView;
import be.wts.lottosysteem_Ali.exception.GebruikerNietGevondenException;
import be.wts.lottosysteem_Ali.model.Gebruiker;
import be.wts.lottosysteem_Ali.repository.GebruikerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GebruikerService {
    private final GebruikerRepository gebruikerRepository;
    private final PasswordEncoder passwordEncoder;

    public GebruikerService(GebruikerRepository gebruikerRepository, PasswordEncoder passwordEncoder) {
        this.gebruikerRepository = gebruikerRepository;
        this.passwordEncoder = passwordEncoder;
    }

   public Optional<Gebruiker> findByGebruikersnaam(String gebruikersnaam) {
        return gebruikerRepository.findByGebruikersnaam(gebruikersnaam);
   }

   public long create(Gebruiker gebruiker) {
        String Rol = (gebruiker.getRol() == null || gebruiker.getRol().isBlank())
                ? "USER"
                : gebruiker.getRol().toUpperCase();

        String hashedWachtwoord = passwordEncoder.encode(gebruiker.getWachtwoord());
        var gebruikerMetHash = new Gebruiker(
                gebruiker.getId(),
                gebruiker.getGebruikersnaam(),
                hashedWachtwoord,
                Rol
        );
        return gebruikerRepository.save(gebruikerMetHash);
   }

   public long updateWachtwoord(long id, String nieuwWachtwoord) {
        String hashedWachtwoord = passwordEncoder.encode(nieuwWachtwoord);
        return gebruikerRepository.updateWachtwoord(id, hashedWachtwoord);
   }

   public boolean isWachtwoordCorrect(String gebruikersnaam, String ingevoerdWachtwoord) {
        Optional<Gebruiker> gebruikerOpt = gebruikerRepository.findByGebruikersnaam(gebruikersnaam);
        return gebruikerOpt.isPresent() &&
                passwordEncoder.matches(ingevoerdWachtwoord, gebruikerOpt.get().getWachtwoord());
   }

   public void delete(long id) {
        gebruikerRepository.delete(id);
   }

   public List<GebruikerView> findAllViews() {
        return gebruikerRepository.findAllViews();
   }

    public int updateRol(long id, String rol) {
        return gebruikerRepository.updateRol(id, rol);
    }

    @Transactional
    public Gebruiker wijzigEigenGebruikersnaam(String huidigeGebruikersnaam, String nieuweGebruikersnaam) {
        Gebruiker ingelogde = gebruikerRepository.findByGebruikersnaam(huidigeGebruikersnaam)
                .orElseThrow(GebruikerNietGevondenException::new);

        String nieuwe = nieuweGebruikersnaam == null ? "" : nieuweGebruikersnaam.trim();
        if (nieuwe.length() < 2) {
            throw new IllegalArgumentException("Naam is te kort");
        }
        if (nieuwe.equals(ingelogde.getGebruikersnaam())) {
            return ingelogde; // niets te doen
        }
        if (gebruikerRepository.gebruikersnaamBestaat(nieuwe)) {
            throw new IllegalArgumentException("Gebruikersnaam bestaat al");
        }

        int rows = gebruikerRepository.updateGebruikersnaamById(ingelogde.getId(), nieuwe);
        if (rows == 0) throw new IllegalStateException("Update mislukt");

        // lokale kopie bijwerken voor return
        return new Gebruiker(
                ingelogde.getId(),
                nieuwe,
                ingelogde.getWachtwoord(),
                ingelogde.getRol()
        );
    }
}
