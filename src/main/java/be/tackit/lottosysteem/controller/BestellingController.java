package be.tackit.lottosysteem.controller;

import be.tackit.lottosysteem.dto.BestellingView;
import be.tackit.lottosysteem.dto.NieuweBestelling;
import be.tackit.lottosysteem.dto.UpdateBetaald;
import be.tackit.lottosysteem.model.Bestelling;
import be.tackit.lottosysteem.service.BestellingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bestelling")
public class BestellingController {
    private final BestellingService bestellingService;

    public BestellingController(BestellingService bestellingService) {
        this.bestellingService = bestellingService;
    }

    @GetMapping
    public List<BestellingView> getAllBestellingen() {
        return bestellingService.getAllBestelling().stream()
                .map(this::toView)
                .toList();
    }

    private BestellingView toView(Bestelling b) {
        // Data is nu al aanwezig via JOINs
        var klantNaam = b.getKlant().getNaam();
        if (klantNaam == null)
            klantNaam = "Klant #" + b.getKlant().getId();

        var medewerkerNaam = b.getMedewerkerNaam();
        if (medewerkerNaam == null)
            medewerkerNaam = "#" + b.getMedewerkerId();

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
                b.getLaatsteUpdate());
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

    @GetMapping("/klant/{klantId}")
    public List<BestellingView> getBestellingenVoorKlant(@PathVariable long klantId) {
        return bestellingService.getAllBestellingenVoorKlant(klantId).stream()
                .map(this::toView)
                .toList();
    }
}
