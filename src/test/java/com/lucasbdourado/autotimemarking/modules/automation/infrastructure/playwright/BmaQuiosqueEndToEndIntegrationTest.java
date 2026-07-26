package com.lucasbdourado.autotimemarking.modules.automation.infrastructure.playwright;

import com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config.BmaquiosqueProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BmaQuiosqueEndToEndIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PlaywrightTimeClockClient timeClockClient;

    @Autowired
    private BmaquiosqueProperties properties;

    @DynamicPropertySource
    static void setDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("bmaquiosque.username", () -> "365");
        registry.add("bmaquiosque.password", () -> "pass.test");
        registry.add("bmaquiosque.max-entry-time", () -> "09:00");
        registry.add("bmaquiosque.jitter-minutes", () -> "0");
        registry.add("bmaquiosque.selectors.username", () -> "#Usuario");
        registry.add("bmaquiosque.selectors.password", () -> "#Senha");
        registry.add("bmaquiosque.selectors.login-button", () -> "input[type='submit']");
        registry.add("bmaquiosque.selectors.markings-container", () -> ".marking-time-item");
        registry.add("bmaquiosque.selectors.punch-button", () -> "#btnEfetuarMarcacao");
    }

    @Test
    @DisplayName("Should perform end-to-end Playwright browser login, retrieve markings, and register punch against mock BMA Quiosque server")
    void testEndToEndPlaywrightFlow() throws Exception {
        // Set real local server URL
        properties.setUrl("http://localhost:" + port + "/wpe/quiosque");

        // 1. Retrieve markings (should contain initial "09:00")
        List<LocalTime> initialMarkings = timeClockClient.retrieveDailyMarkings("365", "pass.test");
        assertThat(initialMarkings).contains(LocalTime.of(9, 0));

        // 2. Register punch
        timeClockClient.registerMarking("365", "pass.test");

        // 3. Verify new punch was added
        List<LocalTime> updatedMarkings = timeClockClient.retrieveDailyMarkings("365", "pass.test");
        assertThat(updatedMarkings).hasSize(initialMarkings.size() + 1);
    }
}
