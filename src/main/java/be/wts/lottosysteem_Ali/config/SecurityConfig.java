package be.wts.lottosysteem_Ali.config;

import be.wts.lottosysteem_Ali.repository.GebruikerRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/data/**", "/auth/login", "/img/**").permitAll()
                        .requestMatchers("/index.html", "/welkom.html", "/gebruikerAanmaken.html", "/wachtwoordWijzigen.html").permitAll()
                        .requestMatchers("/gebruiker/**").authenticated()
                        .requestMatchers("/bestelling/**").authenticated()
                        .requestMatchers("/klanten/**").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler((req, resp, auth) -> resp.setStatus(HttpServletResponse.SC_OK))
                );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(GebruikerRepository repo) {
        return gebruikersnaam -> repo.findByGebruikersnaam(gebruikersnaam)
                .map(gebruiker -> User.withUsername(gebruiker.getGebruikersnaam())
                        .password(gebruiker.getWachtwoord()) // hash uit DB
                        .roles(gebruiker.getRol())            // bv. ADMIN, USER
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Gebruiker niet gevonden"));
    }

//    @Bean
//    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
//        return http.getSharedObject(AuthenticationManagerBuilder.class)
//                .build();
//    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
