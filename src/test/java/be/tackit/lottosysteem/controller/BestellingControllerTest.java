package be.tackit.lottosysteem.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import be.tackit.lottosysteem.service.PrintService;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Sql("/testdataGebruikers/gebruikers.sql")
@WithMockUser(username = "jan")

public class BestellingControllerTest {
    private final MockMvcTester mockMvcTester;
    private final JdbcClient jdbcClient;
    private final String BESTELLINGEN_TABLE =  "bestelling";

    @MockitoBean
    @SuppressWarnings("unused")
    private PrintService printService;

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

    @Test
    void addBestellingVoegtBestellingToe() {
        jdbcClient.sql("insert into klant(naam, email) values (:naam, :email)")
                .param("naam", "TestKlant_" + System.nanoTime())
                .param("email", "test@tack.it")
                .update();

        long klantId = jdbcClient.sql("select max(id) from klant")
                .query(Long.class)
                .single();

        var json = """
        {
          "klantId": %d,
          "spelType": "Lotto",
          "maand": "juli",
          "betaald": true
        }
        """.formatted(klantId);

        var aantalVoor = JdbcTestUtils.countRowsInTable(jdbcClient, BESTELLINGEN_TABLE);

        var response = mockMvcTester.post()
                .uri("/bestelling")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();

        assertThat(response).hasStatusOk();
        assertThat(JdbcTestUtils.countRowsInTable(jdbcClient, BESTELLINGEN_TABLE))
                .isEqualTo(aantalVoor + 1);
    }



    @Test
    void addBestellingZonderKlantIdMislukt() throws Exception {
        var json = new ClassPathResource("/bestellingTestData/bestellingZonderKlant.json")
                .getContentAsString(StandardCharsets.UTF_8);
        var response = mockMvcTester.post()
                .uri("/bestelling")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void addBestellingMetNegatiefKlantId() throws Exception {
        var json = new ClassPathResource("/bestellingTestData/bestellingMetNegatieveKlantId.json")
                .getContentAsString(StandardCharsets.UTF_8);
        var response = mockMvcTester.post()
                .uri("/bestelling")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void addBestellingMetLeegSpeltype() throws Exception {
        var json = new ClassPathResource("/bestellingTestData/bestellingZonderSpeltype.json")
                .getContentAsString(StandardCharsets.UTF_8);
        var response = mockMvcTester.post()
                .uri("/bestelling")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void addBestellingMetOngeldigSpeltypeMislukt() throws Exception {
    var json = new ClassPathResource("/bestellingTestData/bestellingMetOngeldigSpeltype.json")
    .getContentAsString(StandardCharsets.UTF_8);

    var response = mockMvcTester.post()
            .uri("/bestelling")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);
    assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void addBestellingZonderDatumRegistratieMislukt() throws Exception {
        var json = new ClassPathResource("/bestellingTestData/bestellingZonderDatumRegistratie.json")
                .getContentAsString(StandardCharsets.UTF_8);
        var response = mockMvcTester.post()
                .uri("/bestelling")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
    }

}
