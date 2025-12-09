package be.tackit.lottosysteem.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
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

//    @Test
//    void findAllGeeftAlleBestellingen() {
//        var response = mockMvcTester.get()
//                .uri("/bestelling");
//        assertThat(response).hasStatusOk()
//                .bodyJson()
//                .extractingPath("length()")
//                .isEqualTo(JdbcTestUtils.countRowsInTable(jdbcClient, BESTELLINGEN_TABLE));
//
//    }

//    @Transactional
//    @Test
//    void addBestellingVoegtBestellingToe() throws Exception {
//        var json = new ClassPathResource("/bestellingTestData/correcteBestelling.json")
//                .getContentAsString(StandardCharsets.UTF_8);
//        var aantalVoor = JdbcTestUtils.countRowsInTable(jdbcClient, BESTELLINGEN_TABLE);
//        var response = mockMvcTester.post()
//                .uri("/bestelling")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(json);
//        assertThat(response)
//                .hasStatusOk()
//                .bodyJson()
//                .extractingPath("$")
//                .isInstanceOf(Number.class);
//        assertThat(JdbcTestUtils.countRowsInTable(jdbcClient, BESTELLINGEN_TABLE)).isEqualTo(aantalVoor + 1);
//    }

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
