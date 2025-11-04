package be.wts.lottosysteem_Ali.controller;

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
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class KlantControllerTest {
    private final MockMvcTester mockMvcTester;
    private final JdbcClient jdbcClient;
    private final String KLANTEN_TABLE = "klant";

    public KlantControllerTest(MockMvcTester mockMvcTester, JdbcClient jdbcClient) {
        this.mockMvcTester = mockMvcTester;
        this.jdbcClient = jdbcClient;
    }

    @Test
    void findAllGeeftAlleKlanten(){
        var response = mockMvcTester.get()
                .uri("/klanten");
        assertThat(response).hasStatusOk()
                .bodyJson()
                .extractingPath("length()")
                .isEqualTo(JdbcTestUtils.countRowsInTable(jdbcClient, KLANTEN_TABLE));

    }

    @Transactional
    @Test
    void createVoegtNieuweKlantToe() throws Exception {
        var json = new ClassPathResource("/klantTestData/CorrecteKlant.json")
                .getContentAsString(StandardCharsets.UTF_8);
        var aantalVoor = JdbcTestUtils.countRowsInTable(jdbcClient, KLANTEN_TABLE);

        var response = mockMvcTester.post()
                .uri("/klanten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response).hasStatusOk()
                .bodyJson()
                .extractingPath("$")
                .isInstanceOf(Number.class);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcClient, KLANTEN_TABLE)).isEqualTo(aantalVoor + 1);

    }

    @Test
    void createZonderNaamMislukt() throws Exception {
        var json = new ClassPathResource("/klantTestData/KlantZonderNaam.json")
                .getContentAsString(StandardCharsets.UTF_8);

        var response = mockMvcTester.post()
                .uri("/klanten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createZonderEmailMislukt() throws Exception {
        var json = new ClassPathResource("/klantTestData/KlantZonderEmail.json")
                .getContentAsString(StandardCharsets.UTF_8);
        var response = mockMvcTester.post()
                .uri("/klanten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void findByNaamBegintMetGeeftLegeLijstBijGeenMatch() throws Exception {
        var response = mockMvcTester.get().uri("/klanten/zoeken/xyzOnbestaandeKlant");
        assertThat(response).hasStatusOk()
                .bodyJson()
                .extractingPath("length()")
                .isEqualTo(0);
    }

    @Test
    void findByIdMetOnbestaandIdGeeftLeegResultaat() throws Exception {
        var response = mockMvcTester.get()
                .uri("/klanten/999999");
        assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
    }
}
