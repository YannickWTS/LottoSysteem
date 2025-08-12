package be.wts.lottosysteem_Ali.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class KlantRepository {
    private final JdbcClient jdbcClient;
    public KlantRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }


}
