package be.tackit.lottosysteem.backup;

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
            // 1) Vind het .mv.db bestand om de 'backups' map te bepalen
            Path mvDb = resolveMvDbPathFromH2Url(datasourceUrl);
            if (mvDb == null) {
                log.warn("Backup skipped: kon mv.db path niet afleiden uit datasource url: {}", datasourceUrl);
                return;
            }

            // 2) Maak backups folder
            Path backupsDir = mvDb.getParent().resolve("backups");
            Files.createDirectories(backupsDir);

            // 3) Bepaal maandnaam -> backup-YYYY-MM.zip (H2 backup is ZIP standaard)
            String ym = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Path backupFile = backupsDir.resolve("backup-" + ym + ".zip");

            // 4) Voer BACKUP TO command uit via SQL
            // Dit laat H2 zelf de backup maken, wat locking issues voorkomt
            log.info("Starting database backup to: {}", backupFile);

            String sqlVal = backupFile.toAbsolutePath().toString().replace('\\', '/');
            String sql = "BACKUP TO '" + sqlVal + "'";

            try (java.sql.Connection conn = dataSource.getConnection();
                    java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }

            log.info("Database backup OK -> {}", backupFile);
            System.out.println("BACKUP SUCCESSFUL: " + backupFile);

        } catch (Exception e) {
            // Nooit de shutdown breken
            log.error("Database backup FAILED (shutdown gaat verder): {}", e.getMessage(), e);
            System.err.println("BACKUP FAILED: " + e.getMessage());
        } finally {
            // Forceer JVM stop. PrintService kan AWT threads starten die JVM open houden.
            // We gebruiken een aparte thread en halt(0) om deadlocks in shutdown hooks te
            // vermijden.
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Geef logs even tijd
                    System.out.println("Forcing JVM exit...");
                    Runtime.getRuntime().halt(0);
                } catch (InterruptedException e) {
                    // ignore
                }
            }).start();
        }
    }

    private Path resolveMvDbPathFromH2Url(String url) throws IOException {
        // verwacht iets als: jdbc:h2:file:C:/path/to/lottosysteem
        // mvdb = C:/path/to/lottosysteem.mv.db

        if (url == null)
            return null;

        String prefix = "jdbc:h2:file:";
        int idx = url.indexOf(prefix);
        if (idx < 0)
            return null;

        String rest = url.substring(idx + prefix.length());

        // Knip params weg (na ;)
        int semicolon = rest.indexOf(';');
        if (semicolon >= 0)
            rest = rest.substring(0, semicolon);

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
