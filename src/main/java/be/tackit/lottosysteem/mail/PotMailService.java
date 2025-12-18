package be.tackit.lottosysteem.mail;

import be.tackit.lottosysteem.repository.BestellingRepository;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class PotMailService {

    private final BestellingRepository bestellingRepository;
    private final MailService mailService;

    public PotMailService(BestellingRepository bestellingRepository, MailService mailService) {
        this.bestellingRepository = bestellingRepository;
        this.mailService = mailService;
    }

    public void verstuurPotMail(String maand, String spelType, MultipartFile file) throws MessagingException, IOException {
        List<String> emails = bestellingRepository.findEmailsVoorPotMail(maand, spelType);

        for (String email : emails) {
            mailService.sendPotMailWithAttachment(email, maand, spelType, file);
        }
    }

}
