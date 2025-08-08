package be.wts.lottosysteem_Ali.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Sql("/testdataGebruikers/gebruikers.sql")
public class GebruikerControllerTest {

    private final MockMvcTester tester;
    private final JdbcClient jdbcclient;
    private final String GEBRUIKER_TABLE =  "gebruiker";

    public GebruikerControllerTest(MockMvcTester tester, JdbcClient jdbcclient) {
        this.tester = tester;
        this.jdbcclient = jdbcclient;
    }

    @Test
    void findByGebruikersnaamMetBestaandeGebruikerGeeftGebruikerTerug(){
        var response = tester.get().uri("/gebruiker/admin");
        assertThat(response)
                .hasStatusOk()
                .bodyJson()
                .extractingPath("gebruikersnaam")
                .isEqualTo("admin");
    }

    @Test
    void findByGebruikersnaamMetOnbestaandeGebruikerGeeftNotFount(){
        var response = tester.get().uri("/gebruiker/geenBestaandeGebruiker");
        assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void createVoegtEenGebruikerToe() throws Exception {
        var json = new ClassPathResource("testdataGebruikers/nieuweGebruiker.json")
                .getContentAsString(StandardCharsets.UTF_8);

        var response = tester.post()
                .uri("/gebruiker")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response)
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$")
                .isNotNull();
    }

    @Test
    void createMetLegeGebruikersnaamGeeftBadRequest() throws Exception {
        var json = new ClassPathResource("/testdataGebruikers/legeGebruikersnaam.json")
                .getContentAsString(StandardCharsets.UTF_8);

        var response = tester.post()
                .uri("/gebruiker")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void isWachtwoordCorrectMetCorrecteGegevensGeeftTrue() throws Exception {
        var json = new ClassPathResource("testdataGebruikers/correcteGegevens.json")
                .getContentAsString(StandardCharsets.UTF_8);

        var response = tester.post()
                .uri("/gebruiker/controle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response)
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$")
                .asInstanceOf(BOOLEAN)
                .isTrue();
    }

    @Test
    void isWachtwoordCorrectMetFouteGegevensGeeftFalse() throws Exception {
        var json = new ClassPathResource("testdataGebruikers/verkeerdWachtwoord.json")
                .getContentAsString(StandardCharsets.UTF_8);

        var response = tester.post()
                .uri("/gebruiker/controle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response)
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$")
                .asInstanceOf(BOOLEAN)
                .isFalse();
    }

    @Test
    void isWachtwoordCorrectMetOnbestaandeGebruikerGeeftFalse() throws Exception {
        var json = new ClassPathResource("testdataGebruikers/onbestaandeGebruiker.json")
                .getContentAsString(StandardCharsets.UTF_8);
        var response = tester.post()
                .uri("/gebruiker/controle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response)
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$")
                .asInstanceOf(BOOLEAN)
                .isFalse();
    }

    @Test
    void updateWachtwoordMetBestaandeIdWerkt() throws Exception {
        var json = new ClassPathResource("testdataGebruikers/nieuwWachtwoord.json")
                .getContentAsString(StandardCharsets.UTF_8);
        var response = tester.put()
                .uri("/gebruiker/1/wachtwoord")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response)
        .hasStatusOk();
    }

    @Test
    void updateWachtwoordMetVerkeerdIdGeeftBadRequest() throws Exception {
        var json = new ClassPathResource("testdataGebruikers/nieuwWachtwoord.json")
                .getContentAsString(StandardCharsets.UTF_8);
        var response = tester.put()
                .uri("/gebruiker/9999/wachtwoord")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
    }

}
