package be.tackit.lottosysteem.mail;

import jakarta.mail.MessagingException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("potmail")   // ⬅️ zoals "gebruiker", "klant", ...
public class PotMailController {

    private final PotMailService potMailService;

    public PotMailController(PotMailService potMailService) {
        this.potMailService = potMailService;
    }

    @GetMapping("ping")
    public String ping() {
        return "pong";
    }

    @PostMapping(path = "send", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public void verstuurPotMail(
            @RequestParam("maandCode") String maandCode,
            @RequestParam("maandLabel") String maandLabel,
            @RequestPart("file") MultipartFile file
            // later kan hier ook nog spelType bij
    ) throws MessagingException, IOException {

        potMailService.verstuurPotMail(maandCode, maandLabel, file);
    }
}
