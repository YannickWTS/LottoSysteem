package be.wts.lottosysteem_Ali.controller;

import be.wts.lottosysteem_Ali.dto.BestellingView;
import be.wts.lottosysteem_Ali.dto.NieuweBestelling;
import be.wts.lottosysteem_Ali.dto.UpdateBetaald;
import be.wts.lottosysteem_Ali.model.Bestelling;
import be.wts.lottosysteem_Ali.repository.GebruikerRepository;
import be.wts.lottosysteem_Ali.repository.KlantRepository;
import be.wts.lottosysteem_Ali.service.BestellingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bestelling")
public class BestellingController {
    private final BestellingService bestellingService;
    private final GebruikerRepository gebruikerRepository;
    private final KlantRepository klantRepository;

    public BestellingController(BestellingService bestellingService, GebruikerRepository gebruikerRepository, KlantRepository klantRepository) {
        this.bestellingService = bestellingService;
        this.gebruikerRepository = gebruikerRepository;
        this.klantRepository = klantRepository;
    }

    @GetMapping
    public List<BestellingView> getAllBestellingen() {
        return bestellingService.getAllBestelling().stream()
                .map(this::toView)
                .toList();
    }

    private BestellingView toView(Bestelling b) {
        var klantNaam = klantRepository.findNaamById(b.getKlant().getId())
                .orElse("Klant #" + b.getKlant().getId());
        var medewerkerNaam = gebruikerRepository.findNaamById(b.getMedewerkerId())
                .orElse("#" + b.getMedewerkerId());

        return new BestellingView(
                b.getId(),
                b.getKlant().getId(),
                klantNaam,
                b.getSpelType(),
                b.getMaand(),
                b.getDatumRegistratie(),
                b.isBetaald(),
                b.getMedewerkerId(),
                medewerkerNaam,
                b.getLaatsteUpdate()
        );
    }

    @PostMapping
    public long addBestelling(@RequestBody NieuweBestelling dto) {
        return bestellingService.addBestelling(dto);
    }

    @PutMapping("{id}/betaald")
    public void setBetaald(@PathVariable long id, @RequestBody UpdateBetaald req) {
        bestellingService.setBetaald(id, req.betaald());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    public void deleteBestelling(@PathVariable long id) {
        bestellingService.deleteBestelling(id);
    }
}
