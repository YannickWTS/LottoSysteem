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

    // Iedereen die ingelogd is
    @GetMapping
    public List<Klant> findAll() {
        return klantService.findAll();
    }

    @GetMapping("{id}")
    public Klant findById(@PathVariable long id) {
        return klantService.findById(id);
    }

    // Zoeken op 'bevat' i.p.v. 'begint met'
    @GetMapping("zoeken/{term}")
    public List<Klant> findByNaamBevat(@PathVariable String term) {
        return klantService.findByNaamBevat(term);
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
        klantService.delete(id);
    }
}
