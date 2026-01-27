package be.tackit.lottosysteem.backup;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class DatabaseBackupOnShutdown {

    private static final Logger log = LoggerFactory.getLogger(DatabaseBackupOnShutdown.class);

    private final DataSource dataSource;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    public DatabaseBackupOnShutdown(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PreDestroy
    public void onShutdown() {
        try {
            closeDataSourceIfPossible();

            // 2) Vind het .mv.db bestand uit de datasource URL
            Path mvDb = resolveMvDbPathFromH2Url(datasourceUrl);
            if (mvDb == null) {
                log.warn("Backup skipped: kon mv.db path niet afleiden uit datasource url: {}", datasourceUrl);
                return;
            }

            if (!Files.exists(mvDb)) {
                log.warn("Backup skipped: databasebestand bestaat niet: {}", mvDb);
                return;
            }

            // 3) Maak backups folder
            Path backupsDir = mvDb.getParent().resolve("backups");
            Files.createDirectories(backupsDir);

            // 4) Bepaal maandnaam
            String ym = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Path backupFile = backupsDir.resolve("backup-" + ym + ".mv.db");

            // 5) Kopieer (overschrijven binnen dezelfde maand)
            Files.copy(mvDb, backupFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);

            log.info("Database backup OK -> {}", backupFile);

        } catch (Exception e) {
            // Nooit de shutdown breken
            log.error("Database backup FAILED (shutdown gaat verder): {}", e.getMessage(), e);
        }
    }

    private void closeDataSourceIfPossible() {
        try {
            if (dataSource instanceof HikariDataSource hikari) {
                log.info("Closing HikariDataSource...");
                hikari.close();
            }
        } catch (Exception e) {
            log.warn("Kon DataSource niet netjes sluiten: {}", e.getMessage());
        }
    }

    private Path resolveMvDbPathFromH2Url(String url) throws IOException {
        // verwacht iets als: jdbc:h2:file:C:/path/to/lottosysteem
        // mvdb = C:/path/to/lottosysteem.mv.db

        if (url == null) return null;

        String prefix = "jdbc:h2:file:";
        int idx = url.indexOf(prefix);
        if (idx < 0) return null;

        String rest = url.substring(idx + prefix.length());

        // Knip params weg (na ;)
        int semicolon = rest.indexOf(';');
        if (semicolon >= 0) rest = rest.substring(0, semicolon);

        // H2 laat zowel / als \ toe, wij laten Path het oplossen
        Path base = Paths.get(rest);

        // In sommige setups eindigt het al op .mv.db, anders voeg toe
        String fileName = base.getFileName().toString();
        if (fileName.endsWith(".mv.db")) {
            return base.toAbsolutePath().normalize();
        }
        return Paths.get(base.toString() + ".mv.db").toAbsolutePath().normalize();
    }
}
