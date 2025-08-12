package be.wts.lottosysteem_Ali.repository;

import be.wts.lottosysteem_Ali.model.Klant;
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
                select id, naam, email
                from klant
                order by id
                """;
        return jdbcClient.sql(sql)
                .query(Klant.class)
                .list();
    }

    public Optional<Klant> findById(long id) {
        var sql = """
                select id, naam, email
                from klant
                where id = ?
                """;
        return jdbcClient.sql(sql)
                .param(id)
                .query(Klant.class)
                .optional();
    }

    public long save(Klant klant) {
        var sql = """
                insert into klant(naam, email)
                values (?, ?)
                """;
        var keyholder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
                .params(klant.getNaam(), klant.getEmail())
                .update(keyholder);

        return Objects.requireNonNull(keyholder.getKey()).longValue();
    }

}
