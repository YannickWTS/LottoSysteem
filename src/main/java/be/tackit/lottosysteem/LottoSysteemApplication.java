package be.tackit.lottosysteem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class LottoSysteemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LottoSysteemApplication.class, args);
        // ✅ Tijdelijke output van bcrypt hash
        String rawPassword = "Test01";
        String hashed = new BCryptPasswordEncoder().encode(rawPassword);
        System.out.println("BCRYPT HASH voor 'Test': " + hashed);

    }

}
