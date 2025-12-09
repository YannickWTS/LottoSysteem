package be.tackit.lottosysteem.controller;

import be.tackit.lottosysteem.dto.NieuwWachtwoord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@SpringBootTest
@AutoConfigureMockMvc
@Sql("/testdataGebruikers/gebruikers.sql")
public class GebruikerControllerTest {

    private final MockMvcTester mvc;
    private final ObjectMapper om;

    public GebruikerControllerTest(MockMvcTester mvc, ObjectMapper om) {
        this.mvc = mvc;
        this.om = om;
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void findByGebruikersnaamMetBestaandeGebruikerGeeftGebruikerTerug(){
        var response = mvc.get().uri("/gebruiker/admin");
        assertThat(response)
                .hasStatusOk()
                .bodyJson()
                .extractingPath("gebruikersnaam")
                .isEqualTo("admin");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void findByGebruikersnaamMetOnbestaandeGebruikerGeeftNotFount(){
        var response = mvc.get().uri("/gebruiker/geenBestaandeGebruiker");
        assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createVoegtEenGebruikerToe() throws Exception {
        var json = new ClassPathResource("testdataGebruikers/nieuweGebruiker.json")
                .getContentAsString(StandardCharsets.UTF_8);

        var response = mvc.post()
                .uri("/gebruiker")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange(); // nodig om getResponse() te kunnen gebruiken

        assertThat(response).hasStatus(HttpStatus.CREATED);

        String location = response.getResponse().getHeader("Location");
        assertThat(location)
                .isNotBlank()
                .startsWith("/gebruiker/");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createMetLegeGebruikersnaamGeeftBadRequest() throws Exception {
        var json = new ClassPathResource("/testdataGebruikers/legeGebruikersnaam.json")
                .getContentAsString(StandardCharsets.UTF_8);

        var response = mvc.post()
                .uri("/gebruiker")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void isWachtwoordCorrectMetCorrecteGegevensGeeftTrue() throws Exception {
        var json = new ClassPathResource("testdataGebruikers/correcteGegevens.json")
                .getContentAsString(StandardCharsets.UTF_8);

        var response = mvc.post()
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void isWachtwoordCorrectMetFouteGegevensGeeftFalse() throws Exception {
        var json = new ClassPathResource("testdataGebruikers/verkeerdWachtwoord.json")
                .getContentAsString(StandardCharsets.UTF_8);

        var response = mvc.post()
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void isWachtwoordCorrectMetOnbestaandeGebruikerGeeftFalse() throws Exception {
        var json = new ClassPathResource("testdataGebruikers/onbestaandeGebruiker.json")
                .getContentAsString(StandardCharsets.UTF_8);
        var response = mvc.post()
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateWachtwoordMetBestaandeIdWerkt() throws Exception {
        var json = new ClassPathResource("testdataGebruikers/nieuwWachtwoord.json")
                .getContentAsString(StandardCharsets.UTF_8);
        var response = mvc.put()
                .uri("/gebruiker/1/wachtwoord")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response).hasStatus(NO_CONTENT);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateWachtwoordMetVerkeerdIdGeeftBadRequest() throws Exception {
        var json = new ClassPathResource("testdataGebruikers/nieuwWachtwoord.json")
                .getContentAsString(StandardCharsets.UTF_8);
        var response = mvc.put()
                .uri("/gebruiker/9999/wachtwoord")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateWachtwoordAlsAdminWerkt() throws Exception {
        var req = new NieuwWachtwoord("adminMagDit");

        var resp = mvc.put()
                .uri("/gebruiker/{id}/wachtwoord", 2L) // ID van 'moderator'
                .contentType("application/json")
                .content(om.writeValueAsString(req));

        assertThat(resp).hasStatus(NO_CONTENT);
    }

}
