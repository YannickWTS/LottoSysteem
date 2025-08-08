package be.wts.lottosysteem_Ali.controller;

import be.wts.lottosysteem_Ali.dto.InlogRequest;
import be.wts.lottosysteem_Ali.dto.NieuwWachtwoord;
import be.wts.lottosysteem_Ali.dto.NieuweGebruiker;
import be.wts.lottosysteem_Ali.exception.GebruikerNietGevondenException;
import be.wts.lottosysteem_Ali.exception.WachtwoordUpdateException;
import be.wts.lottosysteem_Ali.model.Gebruiker;
import be.wts.lottosysteem_Ali.service.GebruikerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("gebruiker")
public class GebruikerController {
    private final GebruikerService gebruikerService;
    public GebruikerController(GebruikerService gebruikerService) {
        this.gebruikerService = gebruikerService;
    }

    @PostMapping
    public long create(@RequestBody @Valid NieuweGebruiker nieuweGebruiker){
        var gebruiker = new Gebruiker(0, nieuweGebruiker.gebruikersnaam(), nieuweGebruiker.wachtwoord(), nieuweGebruiker.rol());
        return gebruikerService.create(gebruiker);
    }

    @GetMapping("{gebruikersnaam}")
    public Gebruiker findByGebruikersnaam(@PathVariable String gebruikersnaam){
        return gebruikerService.findByGebruikersnaam(gebruikersnaam)
                .orElseThrow(GebruikerNietGevondenException::new);
    }

    @PutMapping("{id}/wachtwoord")
    public void updateWachtwoord(@PathVariable long id, @RequestBody NieuwWachtwoord nieuwWachtwoord){
        long rijenupdate = gebruikerService.updateWachtwoord(id, nieuwWachtwoord.wachtwoord());
        if(rijenupdate == 0){
            throw new WachtwoordUpdateException(id);
        }
    }

    @PostMapping("controle")
    public boolean isWachtwoordCorrect(@RequestBody InlogRequest request) {
        return gebruikerService.isWachtwoordCorrect(request.gebruikersnaam(), request.wachtwoord());
    }
}
