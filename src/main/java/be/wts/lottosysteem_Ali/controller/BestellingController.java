package be.wts.lottosysteem_Ali.controller;

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
    public List<Bestelling> getAllBestellingen() {
        return bestellingService.getAllBestelling();
    }

    @PostMapping
    public long addBestelling(@RequestBody Bestelling bestelling) {
        return bestellingService.addBestelling(bestelling);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    public void deleteBestelling(@PathVariable long id) {
        bestellingService.deleteBestelling(id);
    }
}
