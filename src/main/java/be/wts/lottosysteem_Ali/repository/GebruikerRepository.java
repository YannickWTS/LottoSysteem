package be.wts.lottosysteem_Ali.repository;


import be.wts.lottosysteem_Ali.dto.GebruikerView;
import be.wts.lottosysteem_Ali.model.Gebruiker;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class GebruikerRepository {
    private final JdbcClient jdbcClient;

    public GebruikerRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Gebruiker> findByGebruikersnaam(String gebruikersnaam) {
        var sql = """
                select id, gebruikersnaam, wachtwoord, rol
                from gebruiker
                where gebruikersnaam = ?
                """;
        return jdbcClient.sql(sql)
                .param(gebruikersnaam)
                .query(Gebruiker.class)
                .optional();
    }

    public long updateWachtwoord(long id, String nieuwWachtwoord) {
        var sql = """
                update gebruiker
                set wachtwoord = ?
                where id = ?
                """;
        return jdbcClient.sql(sql)
                .params(nieuwWachtwoord, id)
                .update();
    }

    public long save(Gebruiker gebruiker) {
        var sql = """
                INSERT INTO gebruiker (gebruikersnaam, wachtwoord, rol)
                VALUES (?, ?, ?)
                """;

        var keyHolder = new GeneratedKeyHolder();

        jdbcClient.sql(sql)
                .params(
                        gebruiker.getGebruikersnaam(),
                        gebruiker.getWachtwoord(),  // Hier wordt al verwacht dat dit een hash is
                        gebruiker.getRol()
                )
                .update(keyHolder);

        return keyHolder.getKey().longValue();
    }

    public void delete(long id) {
        var sql = """
                delete
                from gebruiker
                where id = ?
                """;
        jdbcClient.sql(sql)
                .param(id)
                .update();
    }

    public List<GebruikerView> findAllViews() {
        var sql = """
                select id, gebruikersnaam, rol
                from gebruiker
                order by id
                """;
        return jdbcClient.sql(sql)
                .query(GebruikerView.class)
                .list();
    }

    public int updateRol(Long id, String rol) {
        var sql = """
                update gebruiker
                set rol = ?
                where id = ?
                """;
        return jdbcClient.sql(sql)
                .params(rol, id)
                .update();
    }

    public boolean gebruikersnaamBestaat(String gebruikersnaam) {
        Long count = jdbcClient.sql("""
                select count(*)
                from gebruiker
                where lower(gebruikersnaam) = lower(?)
            """)
                .param(gebruikersnaam)
                .query(Long.class)
                .single();
        return count > 0;
    }

    public int updateGebruikersnaamById(long id, String nieuweGebruikersnaam) {
        return jdbcClient.sql("""
                update gebruiker
                set gebruikersnaam = ?
                where id = ?
            """)
                .params(nieuweGebruikersnaam, id)
                .update();
    }

}
