package be.wts.lottosysteem_Ali.controller;

import be.wts.lottosysteem_Ali.model.Bestelling;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
public class BestellingControllerTest {
    private final MockMvcTester mockMvcTester;
    private final JdbcClient jdbcClient;
    private final String BESTELLINGEN_TABLE =  "bestelling";

    public BestellingControllerTest(MockMvcTester mockMvcTester, JdbcClient jdbcClient) {
        this.mockMvcTester = mockMvcTester;
        this.jdbcClient = jdbcClient;
    }

    @Test
    void findAllGeeftAlleBestellingen() {
        var response = mockMvcTester.get()
                .uri("/bestelling");
        assertThat(response).hasStatusOk()
                .bodyJson()
                .extractingPath("length()")
                .isEqualTo(JdbcTestUtils.countRowsInTable(jdbcClient, BESTELLINGEN_TABLE));

    }

    @Transactional
    @Test
    void addBestellingVoegtBestellingToe() throws Exception {
        var json = new ClassPathResource("/correcteBestelling.json")
                .getContentAsString(StandardCharsets.UTF_8);
        var aantalVoor = JdbcTestUtils.countRowsInTable(jdbcClient, BESTELLINGEN_TABLE);
        var response = mockMvcTester.post()
                .uri("/bestelling")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response)
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$")
                .isInstanceOf(Number.class);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcClient, BESTELLINGEN_TABLE)).isEqualTo(aantalVoor + 1);
    }

    @Test
    void addBestellingZonderKlantIdMislukt() throws Exception {
        var json = new ClassPathResource("/bestellingZonderKlant.json")
                .getContentAsString(StandardCharsets.UTF_8);
        var response = mockMvcTester.post()
                .uri("/bestelling")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
    }
}
