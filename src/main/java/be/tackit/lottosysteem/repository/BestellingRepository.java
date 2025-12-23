package be.tackit.lottosysteem.repository;

import be.tackit.lottosysteem.model.Bestelling;
import be.tackit.lottosysteem.model.Klant;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Repository
public class BestellingRepository {
    private final JdbcClient jdbcClient;

    public BestellingRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Bestelling findById(long id) {
        var sql = """
            select id, klant_id, speltype, maand, datum_registratie, betaald, medewerker_id, laatste_update
            from bestelling
            where id = ?
            """;

        return jdbcClient.sql(sql)
                .param(id)
                .query((rs, rowNum) -> {
                    Timestamp datumTs = rs.getTimestamp("datum_registratie");
                    Timestamp laatsteTs = rs.getTimestamp("laatste_update");

                    return new Bestelling(
                            rs.getLong("id"),
                            new Klant(rs.getLong("klant_id")),
                            rs.getString("speltype"),
                            rs.getString("maand"),
                            datumTs != null ? datumTs.toLocalDateTime() : null,
                            rs.getBoolean("betaald"),
                            rs.getLong("medewerker_id"),
                            laatsteTs != null ? laatsteTs.toLocalDateTime() : null
                    );
                })
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bestelling niet gevonden"));
    }


    public List<Bestelling> findAll() {
        var sql = """
                select id, klant_id, speltype, maand, datum_registratie, betaald, medewerker_id, laatste_update
                from bestelling
                order by id desc
                """;

        return jdbcClient.sql(sql)
                .query((rs, rowNum) -> {
                    Timestamp datumTs = rs.getTimestamp("datum_registratie");
                    Timestamp laatsteTs = rs.getTimestamp("laatste_update");

                    return new Bestelling(
                            rs.getLong("id"),
                            new Klant(rs.getLong("klant_id")),
                            rs.getString("speltype"),
                            rs.getString("maand"),
                            datumTs.toLocalDateTime(), // datum + tijd van bestelling
                            rs.getBoolean("betaald"),
                            rs.getLong("medewerker_id"),
                            laatsteTs != null ? laatsteTs.toLocalDateTime() : null
                    );
                })
                .list();
    }

    public long save(Bestelling bestelling) {
        String sql = """
            INSERT INTO bestelling (klant_id, speltype, maand, datum_registratie, betaald, medewerker_id, laatste_update)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        var keyHolder = new GeneratedKeyHolder();

        // Als direct betaald: laatste_update = datum_registratie (of now)
        // Ik kies datum_registratie = bestelling.getDatumRegistratie() zodat alles consistent is.
        Timestamp laatsteUpdateTs = bestelling.isBetaald()
                ? Timestamp.valueOf(bestelling.getDatumRegistratie())
                : null;

        jdbcClient.sql(sql)
                .params(
                        bestelling.getKlant().getId(),
                        bestelling.getSpelType(),
                        bestelling.getMaand(),
                        Timestamp.valueOf(bestelling.getDatumRegistratie()),
                        bestelling.isBetaald(),
                        bestelling.getMedewerkerId(),
                        laatsteUpdateTs
                )
                .update(keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public int updateBetaald(long id, boolean betaald, long bewerkerId) {
        var sql = """
                    update bestelling
                    set betaald = ?,
                        laatste_update = ?,
                        medewerker_id = ?
                    where id = ?
                """;
        return jdbcClient.sql(sql)
                .params(betaald, Timestamp.valueOf(LocalDateTime.now()), bewerkerId, id)
                .update(); // geeft # rows terug
    }

    public void delete(long id) {
        var sql = """
                delete
                from bestelling
                where id = ?
                """;
        jdbcClient.sql(sql)
                .param(id)
                .update();
    }

    public long countOpenstaandByKlantId(long klantId) {
        var sql = """
            select count(*)
            from bestelling
            where klant_id = ?
              and betaald = false
            """;
        return jdbcClient.sql(sql)
                .param(klantId)
                .query(Long.class)
                .single();
    }


    /**
     * Haalt alle unieke e-mailadressen op van klanten die een betaalde
     * bestelling hebben voor de opgegeven maand en speltype.
     *
     * @param maand bv. "Mei" of "december 2025"
     *              → dit moet exact overeenkomen met wat jij in BESTELLING.MAAND bewaart.
     */
    public List<String> findEmailsVoorPotMail(String maand, String spelType) {
        String sql = """
        select distinct k.email
        from bestelling b
        join klant k on k.id = b.klant_id
        where b.betaald = true
          and b.maand = ?
          and lower(b.speltype) = lower(?)
          and k.email is not null
        """;

        return jdbcClient.sql(sql)
                .param(1, maand)
                .param(2, spelType)
                .query(String.class)
                .list();
    }

    public List<Bestelling> findAllByKlantId(long klantId) {
        var sql = """
            select id, klant_id, speltype, maand, datum_registratie, betaald, medewerker_id, laatste_update
            from bestelling
            where klant_id = ?
            order by datum_registratie desc, id desc
            """;

        return jdbcClient.sql(sql)
                .param(klantId)
                .query((rs, rowNum) -> {
                    Timestamp datumTs = rs.getTimestamp("datum_registratie");
                    Timestamp laatsteTs = rs.getTimestamp("laatste_update");

                    return new Bestelling(
                            rs.getLong("id"),
                            new Klant(rs.getLong("klant_id")),
                            rs.getString("speltype"),
                            rs.getString("maand"),
                            datumTs != null ? datumTs.toLocalDateTime() : null,
                            rs.getBoolean("betaald"),
                            rs.getLong("medewerker_id"),
                            laatsteTs != null ? laatsteTs.toLocalDateTime() : null
                    );
                })
                .list();
    }
}

