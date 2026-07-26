package com.lucasbdourado.autotimemarking.modules.automation;

import com.lucasbdourado.autotimemarking.modules.automation.infrastructure.mockserver.MockCredentialsLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationBehaviorTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MockCredentialsLoader credentialsLoader;

    @Test
    @DisplayName("Verify that MockCredentialsLoader correctly loads username and password from credentials.json")
    void testCredentialsJsonLoading() {
        assertThat(credentialsLoader.getValidUsername()).isEqualTo("365");
        assertThat(credentialsLoader.getValidPassword()).isEqualTo("LucKing@15973");
        assertThat(credentialsLoader.isValidUser("365", "LucKing@15973")).isTrue();
        assertThat(credentialsLoader.isValidUser("365", "senhaErrada123")).isFalse();
    }

    @Test
    @DisplayName("Verify successful login with valid credentials (365 / LucKing@15973)")
    void testSuccessfulLogin() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("Usuario", "365");
        form.add("Senha", "LucKing@15973");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/wpe/quiosque/login",
                new HttpEntity<>(form, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Cartão Ponto");
        assertThat(response.getBody()).doesNotContain("Matrícula ou Senha inválidas");
    }

    @Test
    @DisplayName("Verify login rejection with wrong password (365 / senhaErrada123)")
    void testFailedLoginWrongPassword() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("Usuario", "365");
        form.add("Senha", "senhaErrada123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/wpe/quiosque/login",
                new HttpEntity<>(form, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Matrícula ou Senha inválidas.");
        assertThat(response.getBody()).contains("id=\"retorno\"");
        assertThat(response.getBody()).doesNotContain("Cartão Ponto");
    }

    @Test
    @DisplayName("Verify punch registration success with valid password via AJAX")
    void testPunchRegistrationSuccess() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("Senha", "LucKing@15973");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("X-Requested-With", "XMLHttpRequest");
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/wpe/quiosque/marcacao/registrar",
                new HttpEntity<>(form, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Marcação efetuada com sucesso!");
        assertThat(response.getBody()).contains("success");
    }

    @Test
    @DisplayName("Verify punch registration rejection with wrong password via AJAX")
    void testPunchRegistrationFailedWrongPassword() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("Senha", "senhaErrada123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("X-Requested-With", "XMLHttpRequest");
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/wpe/quiosque/marcacao/registrar",
                new HttpEntity<>(form, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Senha incorreta!");
        assertThat(response.getBody()).contains("error");
    }
}
