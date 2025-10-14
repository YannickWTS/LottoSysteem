package be.wts.lottosysteem_Ali.controller;

import be.wts.lottosysteem_Ali.dto.BestellingView;
import be.wts.lottosysteem_Ali.dto.NieuweBestelling;
import be.wts.lottosysteem_Ali.dto.UpdateBetaald;
import be.wts.lottosysteem_Ali.model.Bestelling;
import be.wts.lottosysteem_Ali.service.BestellingService;
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

    @PostMapping
    public long addBestelling(@RequestBody NieuweBestelling dto) {
        return bestellingService.addBestelling(dto);
    }

    // Alleen ADMIN mag status wijzigen (zoals je eigen regelset stelde)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("{id}/betaald")
    public void setBetaald(@PathVariable long id, @RequestBody UpdateBetaald req) {
        bestellingService.setBetaald(id, req.betaald());
        // hier later trigger voor bon-printen als req.betaald() == true
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    public void deleteBestelling(@PathVariable long id) {
        bestellingService.deleteBestelling(id);
    }

    private BestellingView toView(Bestelling b) {
        return new BestellingView(
                b.getId(),
                b.getKlant().getId(),
                b.getSpelType(),
                b.getMaand(),
                b.getDatumRegistratie(),
                b.isBetaald(),
                b.getMedewerkerId(),
                b.getLaatsteUpdate()
        );
    }
}
