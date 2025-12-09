package be.tackit.lottosysteem.mail;

import jakarta.mail.MessagingException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/pot-mail")
public class PotMailController {

    private final PotMailService potMailService;

    public PotMailController(PotMailService potMailService) {
        this.potMailService = potMailService;
    }

    @PostMapping(path = "/send", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public void verstuurPotMail(
            @RequestParam("maandCode") String maandCode,
            @RequestParam("maandLabel") String maandLabel,
            @RequestPart("file") MultipartFile file
    ) throws MessagingException, IOException {

        potMailService.verstuurPotMail(maandCode, maandLabel, file);
    }
}
