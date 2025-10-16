package be.wts.lottosysteem_Ali.controller;

import be.wts.lottosysteem_Ali.dto.*;
import be.wts.lottosysteem_Ali.exception.GebruikerNietGevondenException;
import be.wts.lottosysteem_Ali.exception.WachtwoordUpdateException;
import be.wts.lottosysteem_Ali.model.Gebruiker;
import be.wts.lottosysteem_Ali.service.GebruikerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("gebruiker")
public class GebruikerController {
    private final GebruikerService gebruikerService;

    public GebruikerController(GebruikerService gebruikerService) {
        this.gebruikerService = gebruikerService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> create(@RequestBody @Valid NieuweGebruiker nieuweGebruiker) {

        var rol = (nieuweGebruiker.rol() == null || nieuweGebruiker.rol().isBlank())
                ? "USER" : nieuweGebruiker.rol().toUpperCase();

        var domein = new Gebruiker(
                0,
                nieuweGebruiker.gebruikersnaam(),
                nieuweGebruiker.wachtwoord(),
                rol
        );

        long id = gebruikerService.create(domein);
        return ResponseEntity.created(URI.create("/gebruiker/" + id)).build();
    }

    @GetMapping("{gebruikersnaam}")
    public Gebruiker findByGebruikersnaam(@PathVariable String gebruikersnaam) {
        return gebruikerService.findByGebruikersnaam(gebruikersnaam)
                .orElseThrow(GebruikerNietGevondenException::new);
    }

    @PutMapping("{id}/wachtwoord")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateWachtwoord(@PathVariable long id,
                                 @RequestBody NieuwWachtwoord nieuwWachtwoord,
                                 Authentication auth) {
        var ingelogdeGebruikersnaam = auth.getName();
        var gebruiker = gebruikerService.findByGebruikersnaam(ingelogdeGebruikersnaam)
                .orElseThrow(GebruikerNietGevondenException::new);

        boolean isEigenAccount = gebruiker.getId() == id;
        boolean isAdmin = gebruiker.getRol().equalsIgnoreCase("ADMIN");

        if (!isEigenAccount && !isAdmin) {
            throw new AccessDeniedException("Geen toegang om dit wachtwoord te wijzigen");
        }

        long rijenupdate = gebruikerService.updateWachtwoord(id, nieuwWachtwoord.wachtwoord());
        if (rijenupdate == 0) {
            throw new WachtwoordUpdateException(id);
        }
    }

    @PostMapping("controle")
    public boolean isWachtwoordCorrect(@RequestBody InlogRequest request) {
        return gebruikerService.isWachtwoordCorrect(request.gebruikersnaam(), request.wachtwoord());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        gebruikerService.delete(id);
    }

    @GetMapping("admin")
    public ResponseEntity<Gebruiker> getIngelogdeGebruiker(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return gebruikerService.findByGebruikersnaam(authentication.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<GebruikerView> findAll() {
        return gebruikerService.findAllViews();
    }

    @PutMapping("{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRol(@PathVariable long id, @RequestBody RolUpdate update) {
        if (update == null || update.rol() == null) {
            throw new IllegalArgumentException("Rol is verplicht");
        }
        var rol = update.rol().toUpperCase();
        if (!rol.equals("ADMIN") && !rol.equals("USER")) {
            throw new IllegalArgumentException("Rol moet USER of ADMIN zijn");
        }

        int rows = gebruikerService.updateRol(id, rol);
        if (rows == 0) {
            throw new GebruikerNietGevondenException(); // of 404 voor onbestaande id
        }
    }

    @PutMapping("mijn-naam")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateMijnGebruikersnaam(
            @RequestBody UpdateUsernameRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        if (request == null || request.gebruikersnaam() == null) {
            return ResponseEntity.badRequest().build();
        }

        // wijzig in DB
        Gebruiker bijgewerkt = gebruikerService
                .wijzigEigenGebruikersnaam(authentication.getName(), request.gebruikersnaam());

        // === SecurityContext verversen (géén herlogin nodig) ===
        var nieuweAuth = new UsernamePasswordAuthenticationToken(
                bijgewerkt.getGebruikersnaam(),
                authentication.getCredentials(),
                authentication.getAuthorities()
        );
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(nieuweAuth);
        SecurityContextHolder.setContext(context);

        httpRequest.getSession(true).setAttribute(
                org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        return ResponseEntity.noContent().build();
    }

}
