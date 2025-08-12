package be.wts.lottosysteem_Ali.controller;

import be.wts.lottosysteem_Ali.model.Klant;
import be.wts.lottosysteem_Ali.service.KlantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    @GetMapping("zoeken/{naamBevat}")
    public List<Klant> findByNaamBegintMet(@PathVariable String naamBevat) {
        return klantService.findByNaamBegintMet(naamBevat);
    }

    @PostMapping
    public long create(@RequestBody @Valid Klant klant) {
        return klantService.save(klant);
    }
}
