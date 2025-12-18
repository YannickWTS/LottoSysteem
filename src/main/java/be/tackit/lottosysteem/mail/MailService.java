package be.tackit.lottosysteem.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Eenvoudige testmail, zonder bijlagen.
     */
    public void sendPlainTextMail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    /**
     * Pot Mail: één mail naar alle ontvangers via BCC, met een afbeelding in bijlage.
     */
    public void sendPotMailWithAttachment(String to, String maand, String spelType, MultipartFile file)
            throws MessagingException, IOException {

        // Speltype mooi labelen
        String spelLabel = switch (spelType.toUpperCase()) {
            case "LOTTO" -> "Lottopot";
            case "LOTTO EXTRA" -> "Lottopot Extra";
            case "EUROMILLIONS" -> "EuroMillions pot";
            case "EUROMILLIONS EXTRA" -> "EuroMillions Extra pot";
            default -> spelType + " pot";
        };

        String subject = spelLabel + " cijfers - " + maand;
        String text = """
        Beste,

        In bijlage vind je de nummers voor de %s van %s.

        Veel succes!
        LottoSysteem – Ali
        """.formatted(spelLabel.toLowerCase(), maand);

        // bestaande code om mail + attachment te bouwen blijft hetzelfde
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, false);

        helper.addAttachment(
                Objects.requireNonNull(file.getOriginalFilename()),
                new ByteArrayResource(file.getBytes())
        );

        mailSender.send(message);
    }
}
