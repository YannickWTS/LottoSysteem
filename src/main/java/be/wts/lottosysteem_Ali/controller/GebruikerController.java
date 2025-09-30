package be.wts.lottosysteem_Ali.controller;

import be.wts.lottosysteem_Ali.dto.InlogRequest;
import be.wts.lottosysteem_Ali.dto.NieuwWachtwoord;
import be.wts.lottosysteem_Ali.dto.NieuweGebruiker;
import be.wts.lottosysteem_Ali.exception.GebruikerNietGevondenException;
import be.wts.lottosysteem_Ali.exception.WachtwoordUpdateException;
import be.wts.lottosysteem_Ali.model.Gebruiker;
import be.wts.lottosysteem_Ali.service.GebruikerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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
    public void updateWachtwoord(@PathVariable long id, @RequestBody NieuwWachtwoord nieuwWachtwoord) {
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
}
