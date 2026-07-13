# Technology Reference: Playwright for Java

## Status

Status: Captured

Last updated: 2026-07-13

Captured by: Antigravity

## Technology Decision Reference

Related technology definition: docs/architecture/auto-time-marking/technology-definition.md

Decision area: Browser Automation

Decision status: Confirmed by user

## Why This Technology Was Selected

Playwright for Java is chosen over Selenium because:
- It runs fast, uses modern DevTools protocols, and offers auto-waiting behavior on locator actions (reducing flaky click/navigation failures).
- It handles browser binary downloads automatically at build/run time, simplifying deployment on server environments.
- It has native support for page screenshots and network request intercepts, which is useful for debugging failures on the headless BMAquiosque site.

## Official Documentation Sources

| Source | URL or Context7 Library ID | Notes |
| --- | --- | --- |
| Context7 | `/microsoft/playwright-java` | Playwright for Java library |
| Official Website | https://playwright.dev/java/ | Official Java documentation |

## Context7 Notes

Playwright requires launching a Playwright process, then launching a Browser instance, creating a BrowserContext (isolated session), and opening a Page.
It is crucial to use the **Try-with-Resources** pattern for browser and context cleanup to prevent memory leaks and orphaned browser processes on the server.
By default, Playwright launches browsers in headless mode. Headless mode can be controlled explicitly using `BrowserType.LaunchOptions`.

## Relevant Concepts for This Project

- **Playwright Lifecycle**: Playwright -> Browser -> BrowserContext -> Page.
- **Locators**: Use `page.locator(selector)` to define elements. Playwright auto-waits for the element to be attached, visible, and stable before clicking or filling.
- **Headless Execution**: Running without a graphical user interface, controlled by `new BrowserType.LaunchOptions().setHeadless(true)`.

## Usage Guidelines for This Project

- Always run Playwright instances within a `try-with-resources` block or manage their lifecycle tied to Spring Boot beans (e.g. closing browser on application shutdown).
- Centralize all BMAquiosque URL paths and CSS/text selectors in a configuration or constant class.
- When an action fails, capture a page screenshot using `page.screenshot(...)` and log it to a dedicated directory for debug analysis.
- Use distinct browser contexts per operation if the system expands to multiple concurrent users, ensuring complete session isolation.

## Examples or Patterns to Follow

### Basic Initialization & Automation (Try-With-Resources)
```java
import com.microsoft.playwright.*;

public class BMAquiosqueAutomation {
    public void runAutomation(String url, String username, String password) {
        try (Playwright playwright = Playwright.create()) {
            BrowserType chromium = playwright.chromium();
            Browser browser = chromium.launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            
            // Navigate
            page.navigate(url);
            
            // Login
            page.locator("input#username").fill(username);
            page.locator("input#password").fill(password);
            page.locator("button#login-btn").click();
            
            // Get page status / punch marking
            // ...
            
            browser.close();
        }
    }
}
```

## Risks or Caveats

- **System Packages**: Running Chromium via Playwright on Linux servers requires additional OS libraries (e.g., `libatk`, `libx11`, etc.). On a bare Linux VM, we must run `mvn exec:java -e -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install-deps"` or install Chromium dependencies manually.
- **Browser Thread Isolation**: Playwright is not thread-safe. All calls to Playwright objects (Browser, Page, etc.) should be performed on the thread that created them, or properly synchronized.

## Related Harness Documents

| Document | Path | Relationship |
| --- | --- | --- |
| Technology Definition | [technology-definition.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/architecture/auto-time-marking/technology-definition.md) | Source decision |
