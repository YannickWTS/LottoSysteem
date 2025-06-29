package be.wts.lottosysteem_Ali.service;

import be.wts.lottosysteem_Ali.model.Gebruiker;
import be.wts.lottosysteem_Ali.repository.GebruikerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        String gehashedWachtwoord = passwordEncoder.encode(gebruiker.getWachtwoord());
        var gebruikerMetHash = new Gebruiker(
                gebruiker.getId(),
                gebruiker.getGebruikersnaam(),
                gehashedWachtwoord,
                gebruiker.getRol()
        );
        return gebruikerRepository.save(gebruikerMetHash);
   }

   public long updateWachtwoord(long id, String nieuwWachtwoord) {
        String gehashedWachtwoord = passwordEncoder.encode(nieuwWachtwoord);
        return gebruikerRepository.updateWachtwoord(id, gehashedWachtwoord);
   }

   public boolean isWachtwoordCorrect(String gebruikersnaam, String ingevoerdwachtwoord) {
        Optional<Gebruiker> gebruikerOpt = gebruikerRepository.findByGebruikersnaam(gebruikersnaam);
        return gebruikerOpt.isPresent() &&
                passwordEncoder.matches(ingevoerdwachtwoord, gebruikerOpt.get().getWachtwoord());
   }
}
