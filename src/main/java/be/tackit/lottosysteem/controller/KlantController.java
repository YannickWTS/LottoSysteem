package be.tackit.lottosysteem.controller;

import be.tackit.lottosysteem.model.Klant;
import be.tackit.lottosysteem.service.KlantService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("klanten")
public class KlantController {
    private final KlantService klantService;

    public KlantController(KlantService klantService) {
        this.klantService = klantService;
    }

    @GetMapping
    public List<Klant> findAll() {
        return klantService.findAll();
    }

    @GetMapping("{id}")
    public Klant findById(@PathVariable long id) {
        return klantService.findById(id);
    }

    @GetMapping("zoeken/{term}")
    public List<Klant> findByNaamBevat(@PathVariable String term) {
        return klantService.findByNaamBevat(term);
    }

    @GetMapping("actief/zoeken/{term}")
    public List<Klant> findActiveByNaamBevat(@PathVariable String term) {
        return klantService.findActiveByNaamBevat(term);
    }

    @PostMapping
    public long create(@RequestBody @Valid Klant klant) {
        return klantService.save(klant);
    }

    @PutMapping("{id}")
    public void update(@PathVariable long id, @RequestBody @Valid Klant klant) {
        klantService.update(id, klant);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable long id) {
        klantService.anonymize(id);
    }
}