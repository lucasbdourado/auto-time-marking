package com.lucasbdourado.autotimemarking.modules.automation.infrastructure.playwright;

import com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config.BmaquiosqueProperties;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaywrightTimeClockClientTest {

    private static HttpServer server;
    private static int port;
    private BmaquiosqueProperties properties;
    private PlaywrightTimeClockClient client;

    @BeforeAll
    static void startHttpServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        port = server.getAddress().getPort();

        server.createContext("/login", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String response = """
                            <!DOCTYPE html>
                            <html>
                            <head><title>Dashboard</title></head>
                            <body>
                              <h1>Dashboard</h1>
                              <div class="marking-time-text">08:00</div>
                              <div class="marking-time-text">12:30</div>
                              <div class="marking-time-text">13:30</div>
                              <button id="btn-punch" onclick="document.body.innerHTML += '<p id=punched>Done</p>'">Punch</button>
                            </body>
                            </html>
                            """;
                    byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } else {
                    String response = """
                            <!DOCTYPE html>
                            <html>
                            <head><title>Login</title></head>
                            <body>
                              <form action="/login" method="POST">
                                <input name="username" type="text" />
                                <input name="password" type="password" />
                                <button type="submit">Login</button>
                              </form>
                            </body>
                            </html>
                            """;
                    byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                }
            }
        });

        server.start();
    }

    @AfterAll
    static void stopHttpServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @BeforeEach
    void setUp() {
        properties = new BmaquiosqueProperties();
        properties.setUsername("testuser");
        properties.setPassword("secret");
        properties.setUrl("http://127.0.0.1:" + port + "/login");

        BmaquiosqueProperties.Selectors selectors = new BmaquiosqueProperties.Selectors();
        selectors.setUsername("input[name='username']");
        selectors.setPassword("input[name='password']");
        selectors.setLoginButton("button[type='submit']");
        selectors.setMarkingsContainer(".marking-time-text");
        selectors.setPunchButton("#btn-punch");
        properties.setSelectors(selectors);

        client = new PlaywrightTimeClockClient(properties);
    }

    @Test
    void shouldRetrieveDailyMarkingsSuccessfully() throws Exception {
        List<LocalTime> markings = client.retrieveDailyMarkings("testuser", "secret");

        assertThat(markings).containsExactly(
                LocalTime.of(8, 0),
                LocalTime.of(12, 30),
                LocalTime.of(13, 30)
        );
    }

    @Test
    void shouldRegisterMarkingSuccessfully() throws Exception {
        client.registerMarking("testuser", "secret");
    }

    @Test
    void shouldCaptureScreenshotAndThrowExceptionOnSelectorTimeout() {
        // Set invalid selector to trigger timeout
        properties.getSelectors().setMarkingsContainer(".non-existent-selector");

        assertThatThrownBy(() -> client.retrieveDailyMarkings("testuser", "secret"))
                .isInstanceOf(Exception.class);

        Path screenshotDir = Paths.get("logs/screenshots");
        assertThat(Files.exists(screenshotDir)).isTrue();
        File[] files = screenshotDir.toFile().listFiles((dir, name) -> name.startsWith("failure-") && name.endsWith(".png"));
        assertThat(files).isNotEmpty();
    }
}
