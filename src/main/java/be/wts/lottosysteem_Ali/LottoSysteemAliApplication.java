package be.wts.lottosysteem_Ali;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class LottoSysteemAliApplication {

    public static void main(String[] args) {
        SpringApplication.run(LottoSysteemAliApplication.class, args);
        // ✅ Tijdelijke output van bcrypt hash
        String rawPassword = "Test";
        String hashed = new BCryptPasswordEncoder().encode(rawPassword);
        System.out.println("BCRYPT HASH voor 'Test': " + hashed);

    }

}
