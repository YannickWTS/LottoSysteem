package be.wts.lottosysteem_Ali.repository;

import be.wts.lottosysteem_Ali.model.Bestelling;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BestellingRepository {
    private final JdbcClient jdbcClient;

    public BestellingRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Bestelling> findAll() {
        var sql = """
                select id, klant_id, speltype, maand, datum_registratie, betaald
                from bestelling
                order by id
                """;
        return jdbcClient.sql(sql)
                .query(Bestelling.class)
                .list();
    }

    public long save(Bestelling bestelling) {
        String sql = """
            INSERT INTO bestelling (klant_id, speltype, maand, datum_registratie, betaald)
            VALUES (?, ?, ?, ?, ?)
            """;

        var keyHolder = new GeneratedKeyHolder();

        jdbcClient.sql(sql)
                .params(
                        bestelling.getKlantId(),
                        bestelling.getSpelType(),
                        bestelling.getMaand(),
                        bestelling.getDatumRegistratie(),
                        bestelling.isBetaald()
                )
                .update(keyHolder);

        return keyHolder.getKey().longValue();
    }
}
