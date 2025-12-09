package be.tackit.lottosysteem.service;

import be.tackit.lottosysteem.model.Gebruiker;
import be.tackit.lottosysteem.repository.GebruikerRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final GebruikerRepository gebruikerRepository;

    public CustomUserDetailsService(GebruikerRepository gebruikerRepository) {
        this.gebruikerRepository = gebruikerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String gebruikersnaam) throws UsernameNotFoundException {
        Gebruiker g = gebruikerRepository.findByGebruikersnaam(gebruikersnaam)
                .orElseThrow(() -> new UsernameNotFoundException("Onbekende gebruiker: " + gebruikersnaam));

        // Rol uit DB -> uppercase -> met ROLE_-prefix voor Spring Security
        String role = (g.getRol() == null || g.getRol().isBlank()) ? "USER" : g.getRol().toUpperCase();
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

        return User.withUsername(g.getGebruikersnaam())
                .password(g.getWachtwoord())          // BCrypt-hash uit DB
                .authorities(List.of(authority))       // bv. ROLE_ADMIN
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}