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
import java.util.List;
import java.util.Objects;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * eenvoudige testmail, zonder bijlagen.
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
    public void sendPotMailWithAttachment(List<String> recipients, String maandLabel, MultipartFile attachment) throws MessagingException, IOException {
        if (recipients == null || recipients.isEmpty()) {
            //niemand heeft betaald voor deze maand? dan geen mail verzenden.
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        //ontvangers via BCC zodat ze elkaar niet kunnen zien.
        helper.setBcc(recipients.toArray(String[]::new));
        String subject = "Lottopot cijfers - " + maandLabel;

        //-> klopt dit wel? de %s
        String body = """
                Beste,
                
                In bijlage vind je de nummers voor de lottopot van %s.
                
                Veel succes!
                LottoSysteem – Ali
                """.formatted(maandLabel);

        helper.setSubject(subject);
        helper.setText(body, false);

        if (attachment != null && !attachment.isEmpty()) {
            helper.addAttachment(
                    Objects.requireNonNull(attachment.getOriginalFilename()),
                    new ByteArrayResource(attachment.getBytes())
            );
        }

        mailSender.send(message);
    }
}
