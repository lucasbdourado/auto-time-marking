package com.lucasbdourado.autotimemarking.modules.automation.infrastructure.playwright;

import com.lucasbdourado.autotimemarking.modules.automation.domain.TimeClockClient;
import com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config.BmaquiosqueProperties;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component("playwrightTimeClockClient")
public class PlaywrightTimeClockClient implements TimeClockClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaywrightTimeClockClient.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final BmaquiosqueProperties properties;

    public PlaywrightTimeClockClient(BmaquiosqueProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<LocalTime> retrieveDailyMarkings(String username, String password) throws Exception {
        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
                try (BrowserContext context = browser.newContext()) {
                    try (Page page = context.newPage()) {
                        page.setDefaultTimeout(15000);
                        try {
                            LOGGER.info("Navigating to BMAquiosque URL for user: {}", username);
                            page.navigate(properties.getUrl());

                            page.fill(properties.getSelectors().getUsername(), username);
                            page.fill(properties.getSelectors().getPassword(), password);
                            page.click(properties.getSelectors().getLoginButton(), new Page.ClickOptions().setForce(true));

                            LOGGER.info("Waiting for markings container selector: {}", properties.getSelectors().getMarkingsContainer());
                            page.waitForSelector(properties.getSelectors().getMarkingsContainer());

                            List<ElementHandle> elements = page.querySelectorAll(properties.getSelectors().getMarkingsContainer());
                            List<LocalTime> markings = new ArrayList<>();
                            for (ElementHandle element : elements) {
                                String text = element.innerText().trim();
                                if (!text.isEmpty()) {
                                    markings.add(parseTime(text));
                                }
                            }
                            markings.sort(Comparator.naturalOrder());
                            LOGGER.info("Successfully retrieved {} markings for user: {}", markings.size(), username);
                            return markings;
                        } catch (Exception exception) {
                            captureScreenshot(page, exception);
                            throw exception;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void registerMarking(String username, String password) throws Exception {
        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
                try (BrowserContext context = browser.newContext()) {
                    try (Page page = context.newPage()) {
                        page.setDefaultTimeout(15000);
                        try {
                            LOGGER.info("Navigating to BMAquiosque URL for punch registration, user: {}", username);
                            page.navigate(properties.getUrl());

                            page.fill(properties.getSelectors().getUsername(), username);
                            page.fill(properties.getSelectors().getPassword(), password);
                            page.click(properties.getSelectors().getLoginButton(), new Page.ClickOptions().setForce(true));

                            LOGGER.info("Checking for punch button selector: {}", properties.getSelectors().getPunchButton());
                            if (page.querySelectorAll(properties.getSelectors().getPunchButton()).isEmpty()) {
                                if (!page.querySelectorAll("a[href*='/marcacao/registrar']").isEmpty()) {
                                    page.click("a[href*='/marcacao/registrar']", new Page.ClickOptions().setForce(true));
                                } else {
                                    String regUrl = properties.getUrl();
                                    if (!regUrl.endsWith("/")) {
                                        regUrl += "/";
                                    }
                                    page.navigate(regUrl + "marcacao/registrar");
                                }
                            }

                            LOGGER.info("Waiting for punch button selector: {}", properties.getSelectors().getPunchButton());
                            page.waitForSelector(properties.getSelectors().getPunchButton());

                            if (!page.querySelectorAll("#formMarcacao #Senha").isEmpty()) {
                                page.fill("#formMarcacao #Senha", password);
                            }

                            page.click(properties.getSelectors().getPunchButton(), new Page.ClickOptions().setForce(true));

                            LOGGER.info("Successfully registered time marking for user: {}", username);
                        } catch (Exception exception) {
                            captureScreenshot(page, exception);
                            throw exception;
                        }
                    }
                }
            }
        }
    }

    private LocalTime parseTime(String text) {
        String cleaned = text.trim();
        if (cleaned.length() > 5) {
            cleaned = cleaned.substring(0, 5);
        }
        return LocalTime.parse(cleaned, TIME_FORMATTER);
    }

    private void captureScreenshot(Page page, Exception exception) {
        if (page != null) {
            try {
                Path screenshotDir = Paths.get("logs/screenshots");
                if (!Files.exists(screenshotDir)) {
                    Files.createDirectories(screenshotDir);
                }
                Path screenshotPath = screenshotDir.resolve("failure-" + System.currentTimeMillis() + ".png");
                page.screenshot(new Page.ScreenshotOptions().setPath(screenshotPath));
                LOGGER.error("Saved failure screenshot to: {}", screenshotPath.toAbsolutePath(), exception);
            } catch (Exception screenshotException) {
                LOGGER.error("Failed to capture failure screenshot", screenshotException);
            }
        }
    }
}
