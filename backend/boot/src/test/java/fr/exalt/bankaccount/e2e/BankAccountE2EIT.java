package fr.exalt.bankaccount.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.exalt.bankaccount.BootApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootApplication.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class BankAccountE2EIT {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate http;

    @Autowired
    ObjectMapper json;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private <T> ResponseEntity<T> post(String path, Object body, Class<T> type) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> req = new HttpEntity<>(body, headers);
        return http.postForEntity(url(path), req, type);
    }

    @Autowired
    private org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping mapping;

    @Test
    void printAllMappings() {
        mapping.getHandlerMethods().forEach((info, method) -> {
            System.out.println(info + " -> " + method.toString());
        });
    }
    // ---------- Scénario 1 : Compte courant (overdraft) “happy path” ----------

    @Test
    @DisplayName("E2E: ouvrir un compte courant, déposer, retirer, lister les opérations")
    void currentAccount_happyPath() throws Exception {
        // 1) Ouvrir un compte courant avec overdraft 100
        var openResp = post("/accounts/current", Map.of("overdraft", new BigDecimal("-100")), String.class);
        assertThat(openResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        JsonNode openJson = json.readTree(openResp.getBody());
        String accountId = openJson.get("id").asText();
        assertThat(accountId).isNotBlank();

        // 2) Dépôt 200
        var depResp = post("/accounts/" + accountId + "/deposit", Map.of("amount", new BigDecimal("200")), String.class);
        assertThat(depResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode depJson = json.readTree(depResp.getBody());
        assertThat(depJson.get("newBalance").decimalValue()).isEqualByComparingTo("200");

        // 3) Retrait 50
        var witResp = post("/accounts/" + accountId + "/withdraw", Map.of("amount", new BigDecimal("50")), String.class);
        assertThat(witResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode witJson = json.readTree(witResp.getBody());
        assertThat(witJson.get("newBalance").decimalValue()).isEqualByComparingTo("150");

        // 4) Lister opérations
        ResponseEntity<String> opsResp = http.getForEntity(url("/accounts/" + accountId + "/operations"), String.class);
        assertThat(opsResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode ops = json.readTree(opsResp.getBody());
        assertThat(ops.isArray()).isTrue();
        assertThat(ops).hasSize(2);
        assertThat(ops.get(0).get("type").asText()).isIn("DEPOSIT","WITHDRAWAL");
        assertThat(ops.get(1).get("type").asText()).isIn("DEPOSIT","WITHDRAWAL");
    }

    // ---------- Scénario 2 : Compte épargne (ceiling appliqué) ----------

    @Test
    @DisplayName("E2E: savings - plafond respecté : dépôt OK puis dépôt refusé")
    void savingsAccount_ceiling() throws Exception {
        // 1) Ouvrir un compte épargne avec plafond 300
        var openResp = post("/accounts/savings", Map.of("ceiling", new BigDecimal("300")), String.class);
        assertThat(openResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String accountId = json.readTree(openResp.getBody()).get("id").asText();

        // 2) Dépôt 200 OK
        var dep1 = post("/accounts/" + accountId + "/deposit", Map.of("amount", new BigDecimal("200")), String.class);
        assertThat(dep1.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 3) Dépôt 150 => dépasse plafond (200 + 150 > 300) => 400 attendu (BusinessRuleViolation)
        var dep2 = post("/accounts/" + accountId + "/deposit", Map.of("amount", new BigDecimal("150")), String.class);
        assertThat(dep2.getStatusCode().value()).isEqualTo(422);
    }

    // ---------- Scénario 3 : Compte courant (overdraft appliqué)

    @Test
    @DisplayName("E2E: current - découvert respecté : retrait OK puis retrait refusé")
    void currentAccount_overdraft() throws Exception {
        // 1) Ouvrir un compte épargne avec plafond 300
        var openResp = post("/accounts/current", Map.of("overdraft", new BigDecimal("-300")), String.class);
        assertThat(openResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String accountId = json.readTree(openResp.getBody()).get("id").asText();

        // 2) retrait 200 OK
        var dep1 = post("/accounts/" + accountId + "/withdraw", Map.of("amount", new BigDecimal("200")), String.class);
        assertThat(dep1.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 3) retrait 150 => dépasse découvert -(200 + 150 > 300) => -400 attendu (BusinessRuleViolation)
        var dep2 = post("/accounts/" + accountId + "/withdraw", Map.of("amount", new BigDecimal("150")), String.class);
        assertThat(dep2.getStatusCode().value()).isEqualTo(422);
    }

    // ---------- Scénario 3 : Erreurs de validation ----------

    @Test
    @DisplayName("E2E: montant négatif => 400")
    void negativeAmount_isBadRequest() {
        // compte courant
        var openResp = post("/accounts/current", Map.of("overdraft", new BigDecimal("0")), String.class);
        String accountId = assertAndExtractId(openResp);

        var bad = post("/accounts/" + accountId + "/deposit", Map.of("amount", new BigDecimal("-1")), String.class);
        assertThat(bad.getStatusCode().value()).isEqualTo(400);
    }

    private String assertAndExtractId(ResponseEntity<String> response) {
        assertThat(response.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK);
        try {
            return json.readTree(response.getBody()).get("id").asText();
        } catch (Exception e) {
            throw new AssertionError("Réponse inattendue: " + response.getBody(), e);
        }
    }

}
