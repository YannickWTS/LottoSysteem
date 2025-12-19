package be.tackit.lottosysteem.controller;

import be.tackit.lottosysteem.dto.NieuwWachtwoord;
import be.tackit.lottosysteem.dto.NieuweGebruiker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest
@AutoConfigureMockMvc //filter aan laten voor échte security
@Sql("/testdataGebruikers/gebruikers.sql")
public class GebruikerControllerSecurityTest {
    private final MockMvcTester mvc;
    private final ObjectMapper om;

    public GebruikerControllerSecurityTest(MockMvcTester mvc, ObjectMapper om) {
        this.mvc = mvc;
        this.om = om;
    }

    //---------create---------

    @Test
    @WithMockUser(username = "jan")
    void createGeeft403AlsUserZonderAdminRol() throws Exception {
        var req = new NieuweGebruiker("sec_user_forbidden", "pw123", null);
        var resp = mvc.post()
                .uri("/gebruiker")
                .contentType("application/json")
                .content(om.writeValueAsString(req));

        assertThat(resp).hasStatus(FORBIDDEN);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createGeeft201EnLocationAlsAdmin() throws Exception {
        var req = new NieuweGebruiker("sec_user_allowed" + UUID.randomUUID(), "pw123", null);
        var resp = mvc.post()
                .uri("/gebruiker")
                .contentType("application/json")
                .content(om.writeValueAsString(req))
                .exchange();

        assertThat(resp).hasStatus(CREATED);

        String location = resp.getResponse().getHeader("Location");
        assertThat(location).isNotBlank();
        assertThat(location).startsWith("/gebruiker/");
    }

    // ------------ DELETE ------------

    // Tip: pas hieronder eventueel het id aan naar iets dat zeker bestaat in jouw test-DB.
    // Als je geen vooraf bekende id hebt, kun je eerst een gebruiker aanmaken (met ADMIN) en daarna deleten.

    @Test
    @WithMockUser(username = "jan")
    void delete_geeft403_alsUserZonderAdminRol() {
        var resp = mvc.delete().uri("/gebruiker/{id}", 99999L);
        assertThat(resp).hasStatus(FORBIDDEN);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void delete_geeft204_alsAdminOokBijOnbestaandId() {
        // Controller geeft 204 en service/repo handelt non-existing best-effort af (idempotent delete is ok)
        var resp = mvc.delete().uri("/gebruiker/{id}", 99999L);
        assertThat(resp).hasStatus(NO_CONTENT);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void delete_204_naEerstAanmaken() throws Exception {
        var uniekeNaam = "tmp_del_" + UUID.randomUUID();
        var req = new NieuweGebruiker(uniekeNaam, "pw123", null); // null => default USER

        var create = mvc.post()
                .uri("/gebruiker")
                .contentType("application/json")
                .content(om.writeValueAsString(req))
                .exchange();

        assertThat(create).hasStatus(CREATED);

        String location = create.getResponse().getHeader("Location");
        long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        var delete = mvc.delete().uri("/gebruiker/{id}", id).exchange();
        assertThat(delete).hasStatus(NO_CONTENT);
    }

    @Test
    @WithMockUser(username = "jan")
    void updateWachtwoordGeeft403AlsUserZonderAdminRol() throws Exception {
        var req = new NieuwWachtwoord("nieuwWachtwoord123");
        var resp = mvc.put()
                .uri("/gebruiker/{id}/wachtwoord", 1L) // jan (id 3) wil admin (id 1) aanpassen
                .contentType("application/json")
                .content(om.writeValueAsString(req));
        assertThat(resp).hasStatus(FORBIDDEN);
    }
}
