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
}
