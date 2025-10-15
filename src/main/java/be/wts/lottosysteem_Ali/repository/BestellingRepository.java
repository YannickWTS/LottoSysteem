package be.wts.lottosysteem_Ali.repository;

import be.wts.lottosysteem_Ali.model.Bestelling;
import be.wts.lottosysteem_Ali.model.Klant;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class BestellingRepository {
    private final JdbcClient jdbcClient;

    public BestellingRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Bestelling> findAll() {
        var sql = """
                select id, klant_id, speltype, maand, datum_registratie, betaald, medewerker_id, laatste_update
                from bestelling
                order by id
                """;
        return jdbcClient.sql(sql)
                .query((rs, rowNum) -> new Bestelling(
                        rs.getLong("id"),
                        new Klant(rs.getLong("klant_id")),
                        rs.getString("speltype"),
                        rs.getString("maand"),
                        rs.getDate("datum_registratie").toLocalDate(),
                        rs.getBoolean("betaald"),
                        rs.getLong("medewerker_id"),
                        rs.getTimestamp("laatste_update").toLocalDateTime()
                ))
                .list();
    }

    public long save(Bestelling bestelling) {
        String sql = """
                INSERT INTO bestelling (klant_id, speltype, maand, datum_registratie, betaald, medewerker_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        var keyHolder = new GeneratedKeyHolder();

        jdbcClient.sql(sql)
                .params(
                        bestelling.getKlant().getId(),
                        bestelling.getSpelType(),
                        bestelling.getMaand(),
                        bestelling.getDatumRegistratie(),
                        bestelling.isBetaald(),
                        bestelling.getMedewerkerId()
                )
                .update(keyHolder);

        return keyHolder.getKey().longValue();
    }

    public int updateBetaald(long id, boolean betaald, long bewerkerId) {
        var sql = """
                    update bestelling
                    set betaald = ?,
                        laatste_update = ?,
                        laatste_bewerker_id = ?
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

    public long countByKlantId(long klantId) {
        var sql = """
                select count(*)
                from bestelling
                where klant_Id = ?
                """;
        return jdbcClient.sql(sql)
                .param(klantId)
                .query(Long.class)
                .single();
    }
}
