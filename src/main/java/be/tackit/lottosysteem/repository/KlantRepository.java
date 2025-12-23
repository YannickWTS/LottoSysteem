package be.tackit.lottosysteem.repository;

import be.tackit.lottosysteem.model.Klant;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class KlantRepository {
    private final JdbcClient jdbcClient;

    public KlantRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Klant> findAll() {
        var sql = """
                select id, naam, email, verwijderd, verwijderd_op
                from klant
                order by verwijderd, lower(naam)
                """;
        return jdbcClient.sql(sql)
                .query(Klant.class)
                .list();
    }

    public Optional<Klant> findById(long id) {
        var sql = """
                select id, naam, email, verwijderd, verwijderd_op
                from klant
                where id = ?
                """;
        return jdbcClient.sql(sql)
                .param(id)
                .query(Klant.class)
                .optional();
    }

    /**
     * Voor algemene zoek/scherm (mag ook verwijderde klanten tonen)
     */
    public List<Klant> findByNaamBevat(String deel) {
        var sql = """
                select id, naam, email, verwijderd, verwijderd_op
                from klant
                where lower(naam) like ?
                order by verwijderd, lower(naam)
                """;
        return jdbcClient.sql(sql)
                .param("%" + deel.toLowerCase() + "%")
                .query(Klant.class)
                .list();
    }

    /**
     * Voor autocomplete / klant selecteren in bestellingen: enkel actieve klanten
     */
    public List<Klant> findActiveByNaamBevat(String deel) {
        var sql = """
                select id, naam, email, verwijderd, verwijderd_op
                from klant
                where verwijderd = false
                  and lower(naam) like ?
                order by lower(naam)
                """;
        return jdbcClient.sql(sql)
                .param("%" + deel.toLowerCase() + "%")
                .query(Klant.class)
                .list();
    }

    public long save(Klant klant) {
        var sql = """
                insert into klant(naam, email, verwijderd, verwijderd_op)
                values (?, ?, false, null)
                """;
        var keyholder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
                .params(klant.getNaam(), klant.getEmail())
                .update(keyholder);

        return Objects.requireNonNull(keyholder.getKey()).longValue();
    }

    public int update(long id, String naam, String email) {
        var sql = """
                update klant
                set naam = ?, email = ?
                where id = ?
                """;
        return jdbcClient.sql(sql)
                .params(naam, email, id)
                .update();
    }

    /**
     * "Verwijderen" = anonimiseren + flagged als verwijderd.
     * We houden id zodat bestellingen/historiek blijven bestaan.
     */
    public int anonymize(long id) {
        var sql = """
                update klant
                set naam = 'Verwijderde klant',
                    email = null,
                    verwijderd = true,
                    verwijderd_op = current_timestamp
                where id = ?
                """;
        return jdbcClient.sql(sql)
                .param(id)
                .update();
    }

    public Optional<String> findNaamById(long id) {
        var sql = """
                select naam
                from klant
                where id = ?
                """;
        return jdbcClient.sql(sql)
                .param(id)
                .query(String.class)
                .optional();
    }

    public boolean existsByEmailIgnoreCase(String email) {
        var sql = """
                select count(*)
                from klant
                where verwijderd = false
                  and lower(email) = lower(?)
                """;
        return jdbcClient.sql(sql)
                .param(email)
                .query(Long.class)
                .single() > 0;
    }

    public boolean existsByEmailIgnoreCaseExcludingId(String email, long excludeId) {
        var sql = """
                select count(*)
                from klant
                where verwijderd = false
                  and lower(email) = lower(?)
                  and id <> ?
                """;
        return jdbcClient.sql(sql)
                .params(email, excludeId)
                .query(Long.class)
                .single() > 0;
    }
}
