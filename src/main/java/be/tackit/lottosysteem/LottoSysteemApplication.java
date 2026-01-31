package be.tackit.lottosysteem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class LottoSysteemApplication {

    public static void main(String[] args) throws Exception {

        Path dbDir = Paths.get(System.getProperty("user.home"), ".lottosysteem");
        Files.createDirectories(dbDir); // zorgt dat map bestaat

        SpringApplication.run(LottoSysteemApplication.class, args);
    }
}
